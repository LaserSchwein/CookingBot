package org.example.bot.commands;

import org.example.bot.EditMessageContainer;
import org.example.bot.database.DatabaseManager;
import org.example.bot.database.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class LanguageCommandTest {

    private LanguageCommand languageCommand;
    private DatabaseManager databaseManager;
    private Update update;
    private Message message;
    private CallbackQuery callbackQuery;
    private org.telegram.telegrambots.meta.api.objects.User telegramUser;

    @BeforeEach
    public void setUp() {
        databaseManager = mock(DatabaseManager.class);
        languageCommand = new LanguageCommand(databaseManager);
        update = mock(Update.class);
        message = mock(Message.class);
        callbackQuery = mock(CallbackQuery.class);
        telegramUser = mock(org.telegram.telegrambots.meta.api.objects.User.class);
    }

    @Test
    @DisplayName("Проверка описания команды /language")
    public void testGetDescription() {
        String expectedDescription = "Select one of the suggested languages:";
        String actualDescription = languageCommand.getDescription();
        assertEquals(expectedDescription, actualDescription, "Описание команды должно быть 'Select one of the suggested languages:'");
    }

    @Test
    @DisplayName("Проверка команды /language")
    public void testGetCommand() {
        String expectedCommand = "/language";
        String actualCommand = languageCommand.getCommand();
        assertEquals(expectedCommand, actualCommand, "Команда должна быть '/language'");
    }

    @Test
    @DisplayName("Проверка создания сообщения с кнопками выбора языка")
    public void testGetContent() {
        when(message.getChatId()).thenReturn(12345L);
        when(update.getMessage()).thenReturn(message);

        SendMessage sendMessage = languageCommand.getContent(update);
        assertEquals("Please select your language:", sendMessage.getText(), "Сообщение должно быть 'Please select your language:'");

        InlineKeyboardMarkup keyboardMarkup = (InlineKeyboardMarkup) sendMessage.getReplyMarkup();
        assertNotNull(keyboardMarkup, "InlineKeyboardMarkup не должен быть null");
        assertFalse(keyboardMarkup.getKeyboard().isEmpty(), "InlineKeyboard должен содержать кнопки");

        assertEquals(5, keyboardMarkup.getKeyboard().size(), "Должно быть 5 кнопок");

        InlineKeyboardButton englishButton = keyboardMarkup.getKeyboard().get(0).get(0);
        assertEquals("English", englishButton.getText(), "Текст кнопки должен быть 'English'");
        assertEquals("language:en", englishButton.getCallbackData(), "CallbackData кнопки должен быть 'language:en'");

        InlineKeyboardButton russianButton = keyboardMarkup.getKeyboard().get(1).get(0);
        assertEquals("Русский", russianButton.getText(), "Текст кнопки должен быть 'Русский'");
        assertEquals("language:ru", russianButton.getCallbackData(), "CallbackData кнопки должен быть 'language:ru'");
    }

    @Test
    @DisplayName("Проверка обработки callback-запросов для выбора языка")
    public void testHandleCallback() {
        when(telegramUser.getId()).thenReturn(12345L);
        when(telegramUser.getUserName()).thenReturn("testuser");
        when(callbackQuery.getFrom()).thenReturn(telegramUser);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(callbackQuery.getData()).thenReturn("language:en");
        when(message.getChatId()).thenReturn(12345L);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(update.hasCallbackQuery()).thenReturn(Boolean.TRUE);
        long userId = 12345L;
        when(databaseManager.getLanguage(userId)).thenReturn("en");

        EditMessageContainer editMessageContainer = languageCommand.handleCallback(update);
        assertNotNull(editMessageContainer, "EditMessageContainer не должен быть null");
        assertEquals("Your language has been set to: en", editMessageContainer.getEditMessageText(), "Сообщение должно быть 'Your language has been set to: en'");

        verify(databaseManager, times(1)).updateLanguage(12345L, "en");
        verify(databaseManager, times(1)).addUser(any(User.class));
    }
}
