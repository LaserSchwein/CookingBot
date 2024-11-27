package org.example.bot.commands;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.example.bot.api.SpoonacularAPI;
import org.example.bot.database.DatabaseManager;
import org.example.bot.database.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecipesCommand implements Command {
    private final SpoonacularAPI spoonacularAPI;
    private final DatabaseManager databaseManager;
    private boolean waitingForIngredients = false;
    private long chatId;

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
        long chatId;
        String userInput;

        if (update.hasMessage() && update.getMessage().hasText()) {
            chatId = update.getMessage().getChatId();
            userInput = update.getMessage().getText();
        } else {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            userInput = update.getCallbackQuery().getData();
        }

        if (waitingForIngredients && this.chatId == chatId) {
            waitingForIngredients = false;
            return findRecipes(userInput, chatId);
        } else {
            waitingForIngredients = true;
            this.chatId = chatId;
            return askForIngredients(chatId);
        }
    }

    public SendMessage askForIngredients(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Пожалуйста, укажите ингредиенты, которые у вас есть, через запятую. Например:\nпомидоры, сыр, курица");
        return message;
    }

    private SendMessage findRecipes(String ingredientsInput, long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        try {
            // Получение информации о пользователе из базы данных
            User user = new User(chatId, "");
            user.setVegan(databaseManager.isVegan(chatId));
            user.setVegetarian(databaseManager.isVegetarian(chatId));
            user.setHasAllergies(databaseManager.hasAllergies(chatId));
            user.setAllergies(databaseManager.getAllergies(chatId));

            // Формирование параметров для запроса
            String diet = "";
            if (user.isVegan()) {
                diet = "vegan";
            } else if (user.isVegetarian()) {
                diet = "vegetarian";
            }

            String intolerances = user.getAllergies();

            // Поиск рецептов с помощью Spoonacular API
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
            message.setText("Произошла ошибка при запросе рецептов. Попробуйте позже.");
            e.printStackTrace();
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
                    int id = node.get("id").asInt();
                    recipeIds.add(id);
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
                    String title = node.get("title").asText();
                    recipeTitles.add(title);
                    count++;
                }
            }
        }

        return recipeTitles;
    }


    public InlineKeyboardMarkup createRecipeSelectionKeyboard(List<String> recipeTitles, List<Integer> recipeIds) {
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

    public SendMessage getRecipeInstructions(int recipeId, long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        try {
            // Получение информации о рецепте
            String response = spoonacularAPI.getRecipeInformation(recipeId);
            String instructions = parseRecipeInstructions(response);

            message.setText("Пошаговая инструкция для приготовления:\n" + instructions);
        } catch (Exception e) {
            message.setText("Произошла ошибка при получении инструкции по приготовлению рецепта. Попробуйте позже.");
            e.printStackTrace();
        }

        return message;
    }

    private String parseRecipeInstructions(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        StringBuilder instructions = new StringBuilder();

        if (rootNode.has("instructions")) {
            String htmlInstructions = rootNode.get("instructions").asText();
            // Преобразование HTML в текст
            String plainText = Jsoup.parse(htmlInstructions).text();
            instructions.append(plainText);
        } else if (rootNode.has("analyzedInstructions")) {
            JsonNode analyzedInstructions = rootNode.get("analyzedInstructions");
            if (analyzedInstructions.isArray() && analyzedInstructions.size() > 0) {
                JsonNode steps = analyzedInstructions.get(0).get("steps");
                if (steps.isArray()) {
                    for (JsonNode step : steps) {
                        instructions.append(step.get("number").asInt())
                                .append(". ")
                                .append(step.get("step").asText())
                                .append("\n");
                    }
                }
            }
        }

        return instructions.toString();
    }
}
