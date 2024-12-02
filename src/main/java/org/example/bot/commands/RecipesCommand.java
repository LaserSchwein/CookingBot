package org.example.bot.commands;

import org.example.bot.EditMessageContainer;
import org.example.bot.api.SpoonacularAPI;
import org.example.bot.database.DatabaseManager;
import org.example.bot.database.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecipesCommand implements Command {
    private final SpoonacularAPI spoonacularAPI;
    private final DatabaseManager databaseManager;
    private boolean waitingForIngredients = false;
    private static final Logger logger = Logger.getLogger(RecipesCommand.class.getName());

    public RecipesCommand(SpoonacularAPI api, DatabaseManager dbManager) {
        this.spoonacularAPI = api;
        this.databaseManager = dbManager;
    }

    @Override
    public String getDescription() {
        return "Подобрать рецепты на основе ваших предпочтений.";
    }

    @Override
    public String getCommand() {
        return "/recipes";
    }

    @Override
    public SendMessage getContent(Update update) {
        String userInput;

        long chatId;
        if (update.hasMessage() && update.getMessage().hasText()) {
            chatId = update.getMessage().getChatId();
            userInput = update.getMessage().getText();
        } else {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            userInput = update.getCallbackQuery().getData();
        }

        if (waitingForIngredients) {
            waitingForIngredients = false;
            return findRecipes(userInput, chatId);
        } else {
            waitingForIngredients = true;
            return askForIngredients(chatId);
        }
    }

    public SendMessage askForIngredients(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Пожалуйста, укажите ингредиенты, которые у вас есть, через запятую. Например:\nпомидоры, сыр, курица");
        logger.info("Asking for ingredients from user with chatId: " + chatId);
        return message;
    }

    private SendMessage findRecipes(String ingredientsInput, long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        try {
            User user = new User(chatId, "");
            user.setVegan(databaseManager.isVegan(chatId));
            user.setVegetarian(databaseManager.isVegetarian(chatId));
            user.setHasAllergies(databaseManager.hasAllergies(chatId));
            user.setAllergies(databaseManager.getAllergies(chatId));

            String diet = "";
            if (user.isVegan()) {
                diet = "vegan";
            } else if (user.isVegetarian()) {
                diet = "vegetarian";
            }

            String intolerances = "";
            if (user.hasAllergies()) {
                intolerances = user.getAllergies();
            }

            String response = spoonacularAPI.searchRecipes(ingredientsInput, diet, intolerances);
            List<String> recipeTitles = parseRecipeTitles(response);
            List<Integer> recipeIds = parseRecipeIds(response);

            if (recipeTitles.isEmpty()) {
                message.setText("К сожалению, мы не нашли рецептов, которые соответствуют вашим предпочтениям.");
            } else {
                message.setText("Вот рецепты, которые можно приготовить из указанных ингредиентов:\n" + String.join("\n", recipeTitles));
                message.setReplyMarkup(createRecipeSelectionKeyboard(recipeTitles, recipeIds));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching recipes", e);
            message.setText("Произошла ошибка при запросе рецептов. Попробуйте позже.");
        }

        return message;
    }

    public List<Integer> parseRecipeIds(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        List<Integer> recipeIds = new ArrayList<>();

        if (rootNode.has("results")) {
            JsonNode resultsNode = rootNode.get("results");
            if (resultsNode.isArray()) {
                for (JsonNode node : resultsNode) {
                    JsonNode idNode = node.get("id");
                    if (idNode != null) {
                        int id = idNode.asInt();
                        recipeIds.add(id);
                    }
                }
            }
        }

        return recipeIds;
    }

    public List<String> parseRecipeTitles(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        List<String> recipeTitles = new ArrayList<>();

        if (rootNode.has("results")) {
            JsonNode resultsNode = rootNode.get("results");
            if (resultsNode.isArray()) {
                int count = 0;
                for (JsonNode node : resultsNode) {
                    if (count >= 5) {
                        break;
                    }
                    JsonNode titleNode = node.get("title");
                    if (titleNode != null) {
                        String title = titleNode.asText();
                        recipeTitles.add(title);
                        count++;
                    }
                }
            }
        }

        return recipeTitles;
    }

    public InlineKeyboardMarkup createRecipeSelectionKeyboard(List<String> recipeTitles, List<Integer> recipeIds) {
        if (recipeTitles.isEmpty() || recipeIds.isEmpty()) {
            return null;
        }

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (int i = 0; i < recipeTitles.size(); i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(recipeTitles.get(i));
            button.setCallbackData("recipe_" + recipeIds.get(i));
            rows.add(List.of(button));
        }

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public EditMessageContainer getRecipeInstructions(Update update, int recipeId) {
        String text;

        try {
            String response = spoonacularAPI.getRecipeInformation(recipeId);
            String instructions = parseRecipeInstructions(response);

            text = "Пошаговая инструкция для приготовления:\n" + instructions;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching recipe instructions", e);
            text = "Произошла ошибка при получении инструкции по приготовлению рецепта. Попробуйте позже.";
        }

        return new EditMessageContainer(update,
                text,
                createEmptyKeyboard());
    }

    private String parseRecipeInstructions(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        StringBuilder instructions = new StringBuilder();

        if (rootNode.has("instructions")) {
            JsonNode instructionsNode = rootNode.get("instructions");
            if (instructionsNode != null) {
                String htmlInstructions = instructionsNode.asText();
                String plainText = Jsoup.parse(htmlInstructions).text();
                instructions.append(plainText);
            }
        } else if (rootNode.has("analyzedInstructions")) {
            JsonNode analyzedInstructions = rootNode.get("analyzedInstructions");
            if (analyzedInstructions.isArray() && !analyzedInstructions.isEmpty()) {
                JsonNode steps = analyzedInstructions.get(0).get("steps");
                if (steps.isArray()) {
                    for (JsonNode step : steps) {
                        JsonNode numberNode = step.get("number");
                        JsonNode stepNode = step.get("step");
                        if (numberNode != null && stepNode != null) {
                            instructions.append(numberNode.asInt())
                                    .append(". ")
                                    .append(stepNode.asText())
                                    .append("\n");
                        }
                    }
                }
            }
        }

        return instructions.toString();
    }

    private InlineKeyboardMarkup createEmptyKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
}
