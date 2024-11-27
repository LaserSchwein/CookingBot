package org.example.bot.commands;

import org.example.bot.api.SpoonacularAPI;
import org.example.bot.database.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecipesCommandIntegrationTest {
    private RecipesCommand recipesCommand;
    private SpoonacularAPI spoonacularAPI;
    private DatabaseManager databaseManager;

    @BeforeEach
    public void setUp() {
        spoonacularAPI = Mockito.mock(SpoonacularAPI.class);
        databaseManager = Mockito.mock(DatabaseManager.class);
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

        SendMessage sendMessage = recipesCommand.getContent(update);
        assertEquals("Пожалуйста, укажите ингредиенты, которые у вас есть, через запятую. Например:\nпомидоры, сыр, курица", sendMessage.getText());
    }

    @Test
    public void testGetContentFindRecipes() {
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Mockito.when(update.hasMessage()).thenReturn(true);
        Mockito.when(update.getMessage()).thenReturn(message);
        Mockito.when(message.hasText()).thenReturn(true);
        Mockito.when(message.getChatId()).thenReturn(12345L);
        Mockito.when(message.getText()).thenReturn("banana");

        Mockito.when(databaseManager.isVegan(12345L)).thenReturn(false);
        Mockito.when(databaseManager.isVegetarian(12345L)).thenReturn(false);
        Mockito.when(databaseManager.hasAllergies(12345L)).thenReturn(false);
        Mockito.when(databaseManager.getAllergies(12345L)).thenReturn("");

        String jsonResponse = "{\"results\":[{\"title\":\"Recipe 1\"},{\"title\":\"Recipe 2\"}]}";
        try {
            Mockito.when(spoonacularAPI.searchRecipes("banana", "", "")).thenReturn(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // First call to set waitingForIngredients to true
        recipesCommand.getContent(update);

        SendMessage sendMessage = recipesCommand.getContent(update);
        assertEquals("Вот рецепты, которые можно приготовить из указанных ингредиентов:\nRecipe 1\nRecipe 2", sendMessage.getText());
    }
}
