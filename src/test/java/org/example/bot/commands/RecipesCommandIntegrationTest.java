package org.example.bot.commands;

import okhttp3.OkHttpClient;
import org.example.bot.api.SpoonacularAPI;
import org.example.bot.api.TranslateService;
import org.example.bot.database.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class RecipesCommandIntegrationTest {
    private RecipesCommand recipesCommand;
    private SpoonacularAPI spoonacularAPI;
    private DatabaseManager databaseManager;
    private TranslateService translateService;

    @BeforeEach
    public void setUp() {
        spoonacularAPI = Mockito.mock(SpoonacularAPI.class);
        databaseManager = Mockito.mock(DatabaseManager.class);
        translateService = spy(new TranslateService(new OkHttpClient(), databaseManager));
        recipesCommand = new RecipesCommand(spoonacularAPI, databaseManager);
    }

    @Test
    public void testGetContentAskForIngredients() {
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Mockito.when(update.hasMessage()).thenReturn(true);
        Mockito.when(update.getMessage()).thenReturn(message);
        Mockito.when(message.hasText()).thenReturn(true);
        Mockito.when(message.getChatId()).thenReturn(12345L);
        Mockito.when(message.getText()).thenReturn("/recipes");
        Mockito.when(databaseManager.getLanguage(12345L)).thenReturn("ru");

        doReturn("Пожалуйста, перечислите ингредиенты, которые у вас есть, через запятую. Например:\nпомидоры, сыр, курица")
                .when(translateService).translateFromEnglish(anyString(), eq(12345L));

        SendMessage sendMessage = recipesCommand.getContent(update);
        assertEquals("Пожалуйста, перечислите ингредиенты, которые у вас есть, через запятую. Например:\nпомидоры, сыр, курица", sendMessage.getText());
    }

    @Test
    public void testGetContentFindRecipes() {
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Mockito.when(update.hasMessage()).thenReturn(true);
        Mockito.when(update.getMessage()).thenReturn(message);
        Mockito.when(message.hasText()).thenReturn(true);
        Mockito.when(message.getChatId()).thenReturn(12345L);
        Mockito.when(message.getText()).thenReturn("tomato, cheese, chicken");
        Mockito.when(databaseManager.getLanguage(12345L)).thenReturn("ru");

        Mockito.when(databaseManager.isVegan(12345L)).thenReturn(false);
        Mockito.when(databaseManager.isVegetarian(12345L)).thenReturn(false);
        Mockito.when(databaseManager.hasAllergies(12345L)).thenReturn(false);
        Mockito.when(databaseManager.getAllergies(12345L)).thenReturn("");

        String diet = "";
        if (databaseManager.isVegan(12345L)) {
            diet = "vegan";
        } else if (databaseManager.isVegetarian(12345L)) {
            diet = "vegetarian";
        }

        String jsonResponse = "{\"results\":[{\"title\":\"Recipe 1\"},{\"title\":\"Recipe 2\"}]}";
        try {
            Mockito.when(spoonacularAPI.searchRecipes(message.getText(), diet, databaseManager.getAllergies(12345L))).thenReturn(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        doReturn("Вот несколько рецептов, которые можно приготовить с использованием перечисленных ингредиентов:Рецепт 1Рецепт 2")
                .when(translateService).translateFromEnglish(anyString(), eq(12345L));

        // First call to set waitingForIngredients to true
        recipesCommand.getContent(update);

        SendMessage sendMessage = recipesCommand.getContent(update);
        assertEquals("Вот несколько рецептов, которые можно приготовить с использованием перечисленных ингредиентов:Рецепт 1Рецепт 2", sendMessage.getText());
    }

    @Test
    public void testGetContentFindRecipesWithDiet() {
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Mockito.when(update.hasMessage()).thenReturn(true);
        Mockito.when(update.getMessage()).thenReturn(message);
        Mockito.when(message.hasText()).thenReturn(true);
        Mockito.when(message.getChatId()).thenReturn(12345L);
        Mockito.when(message.getText()).thenReturn("banana");
        Mockito.when(databaseManager.getLanguage(12345L)).thenReturn("ru");

        Mockito.when(databaseManager.isVegan(12345L)).thenReturn(true);
        Mockito.when(databaseManager.isVegetarian(12345L)).thenReturn(true);
        Mockito.when(databaseManager.hasAllergies(12345L)).thenReturn(true);
        Mockito.when(databaseManager.getAllergies(12345L)).thenReturn("banana");

        String jsonResponse = "";
        try {
            Mockito.when(spoonacularAPI.searchRecipes("banana", "vegan", "banana")).thenReturn(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        doReturn("К сожалению, мы не нашли рецептов, соответствующих вашим предпочтениям.")
                .when(translateService).translateFromEnglish(anyString(), eq(12345L));

        // First call to set waitingForIngredients to true
        recipesCommand.getContent(update);

        SendMessage sendMessage = recipesCommand.getContent(update);
        assertEquals("К сожалению, мы не нашли рецептов, соответствующих вашим предпочтениям.", sendMessage.getText());
    }
}
