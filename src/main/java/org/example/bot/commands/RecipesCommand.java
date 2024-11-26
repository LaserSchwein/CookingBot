package org.example.bot.commands;

import org.example.bot.api.SpoonacularAPI;
import org.example.bot.database.DatabaseManager;
import org.example.bot.database.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private SendMessage askForIngredients(long chatId) {
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

            if (recipeTitles.isEmpty()) {
                message.setText("К сожалению, мы не нашли рецептов, которые соответствуют вашим предпочтениям.");
            } else {
                message.setText("Вот рецепты, которые можно приготовить из указанных ингредиентов:\n" + String.join("\n", recipeTitles));
            }
        } catch (Exception e) {
            message.setText("Произошла ошибка при запросе рецептов. Попробуйте позже.");
            e.printStackTrace();
        }

        return message;
    }

    private List<String> parseRecipeTitles(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        List<String> recipeTitles = new ArrayList<>();

        if (rootNode.has("results")) {
            JsonNode resultsNode = rootNode.get("results");
            if (resultsNode.isArray()) {
                for (JsonNode node : resultsNode) {
                    String title = node.get("title").asText();
                    recipeTitles.add(title);
                }
            }
        }

        return recipeTitles;
    }
}
