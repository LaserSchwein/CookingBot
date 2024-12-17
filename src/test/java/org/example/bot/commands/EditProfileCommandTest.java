package org.example.bot.commands;

import org.example.bot.EditMessageContainer;
import org.example.bot.api.TranslateService;
import org.example.bot.database.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class EditProfileCommandTest {

    private DatabaseManager databaseManager;
    private TranslateService translateService;
    private EditProfileCommand editProfileCommand;

    @BeforeEach
    public void setUp() {
        databaseManager = mock(DatabaseManager.class);
        translateService = mock(TranslateService.class);
        editProfileCommand = new EditProfileCommand(databaseManager);
    }

    @Test
    public void testHandleCallbackQueryVegan() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);

        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("edit_vegan");
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(12345L);
        when(translateService.translateFromEnglish(anyString(), anyLong())).thenReturn("Are you vegan?");
        when(databaseManager.getLanguage(12345L)).thenReturn("en");

        EditMessageContainer editMessageContainer = editProfileCommand.handleCallbackQuery(callbackQuery, update, 1);

        assertEquals("Are you vegan?", editMessageContainer.getEditMessageText());

        InlineKeyboardMarkup markup = editMessageContainer.getEditMessageReplyMarkup();
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        assertEquals(1, keyboard.size());
        assertEquals(2, keyboard.get(0).size());
        assertEquals("✅", keyboard.get(0).get(0).getText());
        assertEquals("❎", keyboard.get(0).get(1).getText());
    }

    @Test
    public void testHandleCallbackQueryVegetarian() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);

        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("edit_vegetarian");
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(12345L);
        when(translateService.translateFromEnglish(anyString(), anyLong())).thenReturn("Are you a vegetarian?");
        when(databaseManager.getLanguage(12345L)).thenReturn("en");

        EditMessageContainer editMessageContainer = editProfileCommand.handleCallbackQuery(callbackQuery, update, 1);

        assertEquals("Are you a vegetarian?", editMessageContainer.getEditMessageText());

        InlineKeyboardMarkup markup = editMessageContainer.getEditMessageReplyMarkup();
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        assertEquals(1, keyboard.size());
        assertEquals(2, keyboard.get(0).size());
        assertEquals("✅", keyboard.get(0).get(0).getText());
        assertEquals("❎", keyboard.get(0).get(1).getText());
    }

    @Test
    public void testHandleCallbackQueryAllergies() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);

        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("edit_allergies");
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(12345L);
        when(translateService.translateFromEnglish(anyString(), anyLong())).thenReturn("Do you have any allergies?");
        when(databaseManager.getLanguage(12345L)).thenReturn("en");

        EditMessageContainer editMessageContainer = editProfileCommand.handleCallbackQuery(callbackQuery, update, 1);

        assertEquals("Do you have any allergies?", editMessageContainer.getEditMessageText());

        InlineKeyboardMarkup markup = editMessageContainer.getEditMessageReplyMarkup();
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        assertEquals(1, keyboard.size());
        assertEquals(2, keyboard.get(0).size());
        assertEquals("✅", keyboard.get(0).get(0).getText());
        assertEquals("❎", keyboard.get(0).get(1).getText());
    }

    @Test
    public void testHandleCallbackQueryVeganYes() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);

        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("vegan_yes");
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(12345L);
        when(translateService.translateFromEnglish(anyString(), anyLong())).thenReturn("Your vegan status has been updated.");
        when(databaseManager.getLanguage(12345L)).thenReturn("en");

        EditMessageContainer editMessageContainer = editProfileCommand.handleCallbackQuery(callbackQuery, update, 2);

        assertEquals("Your vegan status has been updated.", editMessageContainer.getEditMessageText());
    }

    @Test
    public void testHandleCallbackQueryVeganNo() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);

        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("vegan_no");
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(12345L);
        when(translateService.translateFromEnglish(anyString(), anyLong())).thenReturn("Your vegan status has been updated.");
        when(databaseManager.getLanguage(12345L)).thenReturn("en");

        EditMessageContainer editMessageContainer = editProfileCommand.handleCallbackQuery(callbackQuery, update, 2);

        assertEquals("Your vegan status has been updated.", editMessageContainer.getEditMessageText());
        verify(databaseManager, times(1)).updateVegan(12345L, false);
    }

    @Test
    public void testHandleCallbackQueryVegetarianYes() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);

        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("vegetarian_yes");
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(12345L);
        long chatId = 12345L;
        when(databaseManager.getLanguage(chatId)).thenReturn("en");
        when(translateService.translateFromEnglish(anyString(), anyLong())).thenReturn("Your vegetarian status has been updated.");

        EditMessageContainer editMessageContainer = editProfileCommand.handleCallbackQuery(callbackQuery, update, 4);

        assertEquals("Your vegetarian status has been updated.", editMessageContainer.getEditMessageText());
    }

    @Test
    public void testHandleCallbackQueryVegetarianNo() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);

        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("vegetarian_no");
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(12345L);
        long chatId = 12345L;
        when(databaseManager.getLanguage(chatId)).thenReturn("en");
        when(translateService.translateFromEnglish(anyString(), anyLong())).thenReturn("Your vegetarian status has been updated.");

        EditMessageContainer editMessageContainer = editProfileCommand.handleCallbackQuery(callbackQuery, update, 4);

        assertEquals("Your vegetarian status has been updated.", editMessageContainer.getEditMessageText());
    }

    @Test
    public void testHandleCallbackQueryAllergiesYes() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);

        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("allergies_yes");
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(12345L);
        when(translateService.translateFromEnglish(anyString(), anyLong())).thenReturn("What allergies do you have?");
        when(databaseManager.hasAllergies(callbackQuery.getMessage().getChatId())).thenReturn(true);
        when(databaseManager.getLanguage(12345L)).thenReturn("en");

        EditMessageContainer editMessageContainer = editProfileCommand.handleCallbackQuery(callbackQuery, update, 6);

        assertEquals("What allergies do you have?", editMessageContainer.getEditMessageText());
    }

    @Test
    public void testHandleCallbackQueryAllergiesNo() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);

        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("allergies_no");
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(12345L);
        when(translateService.translateFromEnglish(anyString(), anyLong())).thenReturn("Your allergy status has been updated.");
        when(databaseManager.getLanguage(12345L)).thenReturn("en");

        EditMessageContainer editMessageContainer = editProfileCommand.handleCallbackQuery(callbackQuery, update, 6);

        assertEquals("Your allergy status has been updated.", editMessageContainer.getEditMessageText());
        verify(databaseManager, times(1)).updateHasAllergies(12345L, false);
    }

    @Test
    public void testHandleCallbackQueryAllergiesDetails() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);

        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("allergies_yes");
        when(callbackQuery.getMessage()).thenReturn(message);
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(12345L);
        long chatId = 12345L;
        when(databaseManager.getLanguage(chatId)).thenReturn("en");
        when(translateService.translateFromEnglish(anyString(), anyLong())).thenReturn("Your allergies have been updated.");
        when(translateService.translateToEnglish(anyString(), anyLong())).thenReturn("Peanuts");

        EditMessageContainer editMessageContainer = editProfileCommand.handleCallbackQuery(callbackQuery, update, 7);

        assertEquals("Your allergies have been updated.", editMessageContainer.getEditMessageText());
    }
}
