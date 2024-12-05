package org.example.bot.commands;

import org.example.bot.api.SpoonacularAPI;
import org.example.bot.database.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
        SendMessage message = recipesCommand.askForIngredients(12345);
        assertEquals("Please list the ingredients you have, separated by commas. For example:\ntomatoes, cheese, chicken", message.getText());
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
