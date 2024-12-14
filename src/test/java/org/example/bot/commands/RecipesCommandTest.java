package org.example.bot.commands;

import okhttp3.OkHttpClient;
import org.example.bot.api.*;
import org.example.bot.database.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecipesCommandTest {
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
    public void testGetDescription() {
        assertEquals("Select recipes based on your preferences.", recipesCommand.getDescription());
    }

    @Test
    public void testGetCommand() {
        assertEquals("/recipes", recipesCommand.getCommand());
    }

    @Test
    public void testAskForIngredients() {
        // Устанавливаем mock данные
        long chatId = 12345L;
        when(databaseManager.getLanguage(chatId)).thenReturn("ru");

        // Create a spy of TranslateService
        TranslateService translateServiceSpy = spy(new TranslateService(new OkHttpClient(), databaseManager));
        RecipesCommand recipesCommandSpy = new RecipesCommand(spoonacularAPI, databaseManager);

        // Mock the translateService to return the expected descriptions
        doReturn("Пожалуйста, перечислите ингредиенты, которые у вас есть, через запятую. Например:помидоры, сыр, курица").when(translateServiceSpy).translateFromEnglish(anyString(), eq(chatId));

        // Вызываем метод и получаем результат
        SendMessage sendMessage = recipesCommandSpy.askForIngredients(chatId);

        // Проверяем результат
        assertEquals("Пожалуйста, перечислите ингредиенты, которые у вас есть, через запятую. Например:\nпомидоры, сыр, курица", sendMessage.getText(), "Текст сообщения должен соответствовать ожидаемому");
        assertEquals(String.valueOf(chatId), sendMessage.getChatId(), "Chat ID должен соответствовать ожидаемому");
    }


    @Test
    public void testParseRecipeTitles() throws IOException {
        String jsonResponse = "{\"results\":[{\"title\":\"Recipe 1\"},{\"title\":\"Recipe 2\"},{\"title\":\"Recipe 3\"},{\"title\":\"Recipe 4\"}]}";
        List<String> recipeTitles = recipesCommand.parseRecipeTitles(jsonResponse);
        assertEquals(4, recipeTitles.size());
        assertEquals("Recipe 1", recipeTitles.get(0));
        assertEquals("Recipe 2", recipeTitles.get(1));
        assertEquals("Recipe 3", recipeTitles.get(2));
        assertEquals("Recipe 4", recipeTitles.get(3));
    }
}
