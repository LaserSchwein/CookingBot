package org.example.bot;

import org.example.bot.commands.*;
import org.example.bot.database.DatabaseManager;
import org.example.bot.api.SpoonacularAPI;
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

public class TelegramBot extends TelegramLongPollingBot {

    private static final LinkedHashMap<String, Command> commands = new LinkedHashMap<>();
    private String botToken;
    private SpoonacularAPI spoonacularAPI;
    private final DatabaseManager databaseManager;
    private Command currentCommand;

    public TelegramBot(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        loadConfig();  // Загружаем токен бота и API токен Spoonacular

        // Регистрация команд
        commands.put("/start", new StartCommand());
        commands.put("/help", new HelpCommand());
        commands.put("/info", new InfoCommand());
        commands.put("/authors", new AuthorsCommand());
        commands.put("/register", new RegisterCommand(databaseManager));
        commands.put("/recipes", new RecipesCommand(spoonacularAPI, databaseManager));
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
                System.err.println("Ошибка: не удалось найти botToken.properties");
                return;
            }
            properties.load(input);

            // Загружаем токен для Telegram бота
            this.botToken = properties.getProperty("bot.token");
            if (this.botToken == null || this.botToken.isEmpty()) {
                throw new IllegalArgumentException("Ошибка: bot.token не найден в botToken.properties");
            }

            // Загружаем токен для SpoonacularAPI
            String spoonacularApiToken = properties.getProperty("spoonacular.api.token");
            if (spoonacularApiToken == null || spoonacularApiToken.isEmpty()) {
                throw new IllegalArgumentException("Ошибка: spoonacular.api.token не найден в botToken.properties");
            }
            this.spoonacularAPI = new SpoonacularAPI(spoonacularApiToken);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка загрузки botToken.properties", e);
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

            // Проверка наличия команды в хеш-таблице
            Command command = commands.get(text);

            if (command != null) {
                int step = databaseManager.getRegistrationStep(update.getMessage().getChatId());

                if (command.getCommand().equals("/register")) {
                    if (step == 3 || step == 5) {
                        sendMessage.setText("Вы уже зарегистрировались");
                    } else {
                        sendMessage = command.getContent(update);
                    }
                } else {
                    sendMessage = command.getContent(update);
                }

                if (command.getCommand().equals("/start")) {
                    sendMessage.setReplyMarkup(((HelpCommand) commands.get("/help")).getReplyKeyboard());
                }

                currentCommand = command;
            } else if (currentCommand instanceof RecipesCommand) {
                sendMessage = currentCommand.getContent(update);
                currentCommand = null;
            } else if (databaseManager.getRegistrationStep(update.getMessage().getChatId()) == 4) {
                RegisterCommand registerCommand = (RegisterCommand) commands.get("/register");

                EditMessageContainer editMessageContainer = registerCommand.registration(update);
                sendMessage.setText(editMessageContainer.getEditMessageText());
            } else {
                sendMessage.setText("Извините, я не понимаю эту команду. Напишите /help для получения списка команд.");
                sendMessage.setReplyMarkup(((HelpCommand) commands.get("/help")).getReplyKeyboard());
            }

            try {
                this.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery(), update);
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery, Update update) {
        String data = callbackQuery.getData();

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(callbackQuery.getMessage().getChatId().toString());
        editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());

        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(callbackQuery.getMessage().getChatId().toString());
        editMarkup.setMessageId(callbackQuery.getMessage().getMessageId());

        if (data.startsWith("recipe_")) {
            int recipeId = Integer.parseInt(data.split("_")[1]);
            // Получите пошаговую инструкцию для рецепта и отправьте её пользователю
            RecipesCommand recipesCommand = (RecipesCommand) commands.get("/recipes");
            EditMessageContainer instructionsMessage = recipesCommand.getRecipeInstructions(update, recipeId);
            editMessageText.setText(instructionsMessage.getEditMessageText());
            editMessageText.setReplyMarkup(instructionsMessage.getEditMessageReplyMarkup());


        } else if (data.equals("/help")) {
            Command helpCommand = commands.get("/help");

            editMessageText.setText(helpCommand.getContent(update).getText());
            editMarkup.setReplyMarkup(((HelpCommand) helpCommand).createInlineCommandsKeyboard());
        } else {
            Command command = commands.get(data);

            if (command != null) {
                currentCommand = command;

                if (command instanceof RegisterCommand) {
                    int step = databaseManager.getRegistrationStep(update.getCallbackQuery().getMessage().getChatId());

                    if (step == 3 || step == 5) {
                        editMessageText.setText("Вы уже зарегистрировались");
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
            this.execute(editMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}