package org.example.bot.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.OkHttpClient;
import org.example.bot.EditMessageContainer;
import org.example.bot.api.SpoonacularAPI;
import org.example.bot.api.TranslateService;
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
    private final TranslateService translateService;
    private long chatId = 1;

    public RecipesCommand(SpoonacularAPI api, DatabaseManager dbManager) {
        this.spoonacularAPI = api;
        this.databaseManager = dbManager;
        OkHttpClient okHttpClient = new OkHttpClient();
        this.translateService = new TranslateService(okHttpClient, databaseManager);
    }

    @Override
    public String getDescription() {
        return "Select recipes based on your preferences.";
    }

    @Override
    public String getCommand() {
        return "/recipes";
    }

    @Override
    public SendMessage getContent(Update update) {
        String userInput;

        if (update.hasMessage() && update.getMessage().hasText()) {
            this.chatId = update.getMessage().getChatId();
            userInput = update.getMessage().getText();
        } else {
            this.chatId = update.getCallbackQuery().getMessage().getChatId();
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
        String text1 = translateService.translateFromEnglish("Please list the ingredients you have, separated by commas. For example:", chatId);
        String text2 = translateService.translateFromEnglish("tomatoes, cheese, chicken", chatId);
        message.setText(text1 + "\n" + text2);
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

            String response = spoonacularAPI.searchRecipes(translateService.translateToEnglish(ingredientsInput, user.getUserId()), diet, intolerances);
            List<String> recipeTitles = parseRecipeTitles(response);
            List<Integer> recipeIds = parseRecipeIds(response);

            if (recipeTitles.isEmpty()) {
                message.setText(translateService.translateFromEnglish("Unfortunately, we did not find any recipes that match your preferences.", chatId));
            } else {
                message.setText(translateService.translateFromEnglish("Here are some recipes that can be made using the ingredients listed:\n" + String.join("\n", recipeTitles), chatId));
                message.setReplyMarkup(createRecipeSelectionKeyboard(recipeTitles, recipeIds, chatId));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching recipes", e);
            message.setText(translateService.translateFromEnglish("There was an error requesting recipes. Please try again later.", chatId));
        }

        return message;
    }

    public List<Integer> parseRecipeIds(String jsonResponse) throws JsonProcessingException {
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

    public InlineKeyboardMarkup createRecipeSelectionKeyboard(List<String> recipeTitles, List<Integer> recipeIds, Long chatId) throws Exception {
        if (recipeTitles.isEmpty() || recipeIds.isEmpty()) {
            return null;
        }

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (int i = 0; i < recipeTitles.size(); i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(translateService.translateFromEnglish(recipeTitles.get(i), chatId));
            button.setCallbackData("recipe_" + recipeIds.get(i));
            rows.add(List.of(button));
        }

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public EditMessageContainer getRecipeInstructions(Update update, int recipeId) throws Exception {
        String text1 = "", text2 = "", text = "";
        try {
            String response = spoonacularAPI.getRecipeInformation(recipeId);
            String instructions = parseRecipeInstructions(response);
            String ingredients = parseIngredientsFromResponse(response);

            text1 = translateService.translateFromEnglish("Step by step instructions for cooking:", this.chatId) + "\n" + instructions  + "\n\n";
            text2 = translateService.translateFromEnglish("Ingredients:", chatId) + "\n" + ingredients;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching recipe instructions", e);
            text1 = "An error occurred while receiving instructions for preparing the recipe. Try again later.";
        }

        return new EditMessageContainer(update,
                text1 + text2,
                createEmptyKeyboard(),
                databaseManager);
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

    private String parseIngredientsFromResponse(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        StringBuilder ingredients = new StringBuilder();

        if (rootNode.has("extendedIngredients")) {
            JsonNode ingredientsNode = rootNode.get("extendedIngredients");
            if (ingredientsNode.isArray()) {
                for (JsonNode ingredientNode : ingredientsNode) {
                    JsonNode nameNode = ingredientNode.get("name");
                    JsonNode amountNode = ingredientNode.get("amount");
                    JsonNode unitNode = ingredientNode.get("unit");
                    if (nameNode != null && amountNode != null && unitNode != null) {
                        ingredients.append(translateService.translateFromEnglish(nameNode.asText(), this.chatId))
                                .append(": ")
                                .append(amountNode.asText())
                                .append(" ")
                                .append(translateService.translateFromEnglish(unitNode.asText(), this.chatId))
                                .append("\n");
                    }
                }
            }
        }

        return ingredients + translateService.translateFromEnglish("To add products to your shopping list, use the", this.chatId) + " / list.";
    }

    private InlineKeyboardMarkup createEmptyKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
}
