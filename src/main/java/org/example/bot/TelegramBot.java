package org.example.bot;

import okhttp3.OkHttpClient;
import org.example.bot.api.TranslateService;
import org.example.bot.commands.*;
import org.example.bot.api.SpoonacularAPI;
import org.example.bot.database.DatabaseManager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TelegramBot extends TelegramLongPollingBot {

    private static final LinkedHashMap<String, Command> commands = new LinkedHashMap<>();
    private final TranslateService translateService;
    private String botToken;
    private SpoonacularAPI spoonacularAPI;
    private final DatabaseManager databaseManager;
    private Command currentCommand;
    private static final Logger logger = Logger.getLogger(TelegramBot.class.getName());

    public TelegramBot(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        OkHttpClient okHttpClient = new OkHttpClient();
        this.translateService = new TranslateService(okHttpClient, databaseManager);
        loadConfig();

        commands.put("/start", new StartCommand());
        commands.put("/help", new HelpCommand(databaseManager));
        commands.put("/info", new InfoCommand(databaseManager));
        commands.put("/authors", new AuthorsCommand(databaseManager));
        commands.put("/register", new RegisterCommand(databaseManager));
        commands.put("/recipes", new RecipesCommand(spoonacularAPI, databaseManager));
        commands.put("/language", new LanguageCommand(databaseManager));
        commands.put("/list", new ListCommand(databaseManager));
        commands.put("/editprofile", new EditProfileCommand(databaseManager));
    }

    public static LinkedHashMap<String, Command> getCommandMap() {
        return commands;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private void loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("botToken.properties")) {
            if (input == null) {
                logger.severe("Error: unable to find botToken.properties");
                return;
            }
            properties.load(input);

            this.botToken = properties.getProperty("bot.token");
            if (this.botToken == null || this.botToken.isEmpty()) {
                throw new IllegalArgumentException("Error: bot.token not found in botToken.properties");
            }

            String spoonacularApiToken = properties.getProperty("spoonacular.api.token");
            if (spoonacularApiToken == null || spoonacularApiToken.isEmpty()) {
                throw new IllegalArgumentException("Error: spoonacular.api.token not found in botToken.properties");
            }
            this.spoonacularAPI = new SpoonacularAPI(spoonacularApiToken);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading botToken.properties", e);
            throw new RuntimeException("Error loading botToken.properties", e);
        }
    }

    @Override
    public String getBotUsername() {
        return "GrandmaIsRecipes_bot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());

            Command command = commands.get(text);

            if (command != null) {
                int step = databaseManager.getRegistrationStep(update.getMessage().getChatId());

                if (command.getCommand().equals("/register")) {
                    if (step == 3 || step == 5) {
                        sendMessage.setText("You have already registered");
                    } else {
                        sendMessage = command.getContent(update);
                    }
                } else {
                    sendMessage = command.getContent(update);
                }

                if (command.getCommand().equals("/start")) {
                    Command languageCommand = commands.get("/language");
                    sendMessage = languageCommand.getContent(update);
                }

                currentCommand = command;
            } else if (currentCommand instanceof ListCommand) {
                ListCommand listCommand = (ListCommand) currentCommand;
                sendMessage = listCommand.processProductInput(update, text);
                currentCommand = null;
            } else if (currentCommand instanceof RecipesCommand) {
                sendMessage = currentCommand.getContent(update);
                currentCommand = null;
            } else if (databaseManager.getRegistrationStep(update.getMessage().getChatId()) == 4) {
                RegisterCommand registerCommand = (RegisterCommand) commands.get("/register");
                EditMessageContainer editMessageContainer = registerCommand.registration(update);
                sendMessage.setText(editMessageContainer.getEditMessageText());
            } else if (databaseManager.getRegistrationStep(update.getMessage().getChatId()) == 7) {
                EditProfileCommand editProfileCommand = (EditProfileCommand) commands.get("/editprofile");

                EditMessageContainer editMessageContainer = editProfileCommand.handleCallbackQuery(new CallbackQuery() ,update, 7);
                sendMessage.setText(editMessageContainer.getEditMessageText());
            }
            else {
                sendMessage.setText(translateService.translateFromEnglish("Sorry, I don't understand this command. Type /help for a list of commands.", update.getMessage().getChatId()));
                sendMessage.setReplyMarkup(((HelpCommand) commands.get("/help")).getReplyKeyboard());
            }

            try {
                this.execute(sendMessage);
            } catch (TelegramApiException e) {
                logger.log(Level.SEVERE, "Error executing sendMessage", e);
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery(), update);
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery, Update update) {
        String data = callbackQuery.getData();
        logger.info("Callback data received: " + data);

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(callbackQuery.getMessage().getChatId().toString());
        editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());

        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(callbackQuery.getMessage().getChatId().toString());
        editMarkup.setMessageId(callbackQuery.getMessage().getMessageId());

        if (data.startsWith("recipe_")) {
            int recipeId = Integer.parseInt(data.split("_")[1]);
            RecipesCommand recipesCommand = (RecipesCommand) commands.get("/recipes");
            EditMessageContainer instructionsMessage;
            try {
                instructionsMessage = recipesCommand.getRecipeInstructions(update, recipeId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            editMessageText.setText(instructionsMessage.getEditMessageText());
            editMessageText.setReplyMarkup(instructionsMessage.getEditMessageReplyMarkup());
        } else if (data.startsWith("language:")) {
            LanguageCommand languageCommand = new LanguageCommand(databaseManager);
            // Отправляем подтверждение
            EditMessageContainer editMessageContainer = languageCommand.handleCallback(update);

            editMessageText.setText(editMessageContainer.getEditMessageText());
            editMarkup.setReplyMarkup(editMessageContainer.getEditMessageReplyMarkup());

        } else if (data.equals("/help")) {
            Command helpCommand = commands.get("/help");
            editMessageText.setText(helpCommand.getContent(update).getText());
            editMarkup.setReplyMarkup(((HelpCommand) helpCommand).createInlineCommandsKeyboard());
        } else if (data.equals("/language")) {
            LanguageCommand languageCommand = new LanguageCommand(databaseManager);
            EditMessageContainer editMessageContainer = languageCommand.handleCallback(update);

            editMessageText.setText(editMessageContainer.getEditMessageText());
            editMarkup.setReplyMarkup(editMessageContainer.getEditMessageReplyMarkup());
        } else if (data.startsWith("edit") || data.equals("/editprofile")) {
            EditProfileCommand editProfileCommand = new EditProfileCommand(databaseManager);
            EditMessageContainer editMessageContainer;
            if (data.equals("/editprofile")) {
                databaseManager.updateRegistrationStep(update.getCallbackQuery().getMessage().getChatId(), 0);
            }
            editMessageContainer = editProfileCommand.handleCallbackQuery(update.getCallbackQuery(), update, databaseManager.getRegistrationStep(update.getCallbackQuery().getMessage().getChatId()));

            editMessageText.setText(editMessageContainer.getEditMessageText());
            System.out.println(editMessageContainer.getEditMessageReplyMarkup());
            editMarkup.setReplyMarkup(editMessageContainer.getEditMessageReplyMarkup());
        } else if (data.startsWith("add_product") || data.startsWith("delete_product") || data.startsWith("view_list")) {
            ListCommand listCommand = (ListCommand) commands.get("/list");
            SendMessage sendMessage = listCommand.handleCallback(update, data);
            editMessageText.setText(sendMessage.getText());
        } else {
            Command command = commands.get(data);
            if (command != null) {
                currentCommand = command;
                if (command instanceof RegisterCommand) {
                    int step = databaseManager.getRegistrationStep(update.getCallbackQuery().getMessage().getChatId());
                    if (step == 3 || step == 5) {
                        editMessageText.setText(translateService.translateFromEnglish("You have already registered", update.getCallbackQuery().getMessage().getChatId()));
                    } else {
                        editMessageText.setText(command.getContent(update).getText());
                    }
                } else {
                    editMessageText.setText(command.getContent(update).getText());
                }
                if (command instanceof HelpCommand) {
                    editMarkup.setReplyMarkup(((HelpCommand) command).createInlineCommandsKeyboard());
                } else if (command instanceof RegisterCommand) {
                    int step = databaseManager.getRegistrationStep(update.getCallbackQuery().getMessage().getChatId());
                    if (step == 3 || step == 5) {
                        editMarkup.setReplyMarkup(command.createHelpBackButtonKeyboard());
                    } else {
                        editMarkup.setReplyMarkup(((RegisterCommand) command).first_Keyboard());
                    }
                } else {
                    editMarkup.setReplyMarkup(command.createHelpBackButtonKeyboard());
                }
            } else if (data.equals("back") || data.startsWith("vegan_") || data.startsWith("vegetarian_") || data.startsWith("allergies_")) {
                RegisterCommand registerCommand = (RegisterCommand) commands.get("/register");
                EditMessageContainer editMessageContainer = registerCommand.registration(update);
                editMessageText.setText(editMessageContainer.getEditMessageText());
                editMarkup.setReplyMarkup(editMessageContainer.getEditMessageReplyMarkup());
            }
        }
        try {
            this.execute(editMessageText);
        } catch (TelegramApiException e) {
            logger.log(Level.SEVERE, "Error executing editMessageText", e);
        }

        if (editMarkup.getReplyMarkup() != null && !editMarkup.getReplyMarkup().getKeyboard().isEmpty()) {
            try {
                this.execute(editMarkup);
            } catch (TelegramApiException e) {
                logger.log(Level.SEVERE, "Error executing editMessageText", e);
            }
        }
    }
}
