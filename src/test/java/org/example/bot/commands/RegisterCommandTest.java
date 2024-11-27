package org.example.bot.commands;

import org.example.bot.EditMessageContainer;
import org.example.bot.TelegramBot;
import org.example.bot.database.DatabaseManager;
import org.example.bot.database.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RegisterCommandTest {

    private RegisterCommand registerCommand;
    private DatabaseManager databaseManager;
    private MockedStatic<TelegramBot> mockedStatic;
    private Update update;
    private Message message;
    private CallbackQuery callbackQuery;
    private org.telegram.telegrambots.meta.api.objects.User telegramUser;

    @BeforeEach
    public void setUp() {
        databaseManager = mock(DatabaseManager.class);
        registerCommand = new RegisterCommand();
        mockedStatic = mockStatic(TelegramBot.class);
        update = mock(Update.class);
        message = mock(Message.class);
        callbackQuery = mock(CallbackQuery.class);
        telegramUser = mock(org.telegram.telegrambots.meta.api.objects.User.class);
    }

    @AfterEach
    public void tearDown() {
        mockedStatic.close();
    }

    @Test
    @DisplayName("Проверка описания команды /register")
    public void testGetDescription() {
        String expectedDescription = "Регистрация аккаунта в боте";
        String actualDescription = registerCommand.getDescription();
        assertEquals(expectedDescription, actualDescription, "Описание команды должно быть 'Регистрация аккаунта в боте'");
    }

    @Test
    @DisplayName("Проверка команды /register")
    public void testGetCommand() {
        String expectedCommand = "/register";
        String actualCommand = registerCommand.getCommand();
        assertEquals(expectedCommand, actualCommand, "Команда должна быть '/register'");
    }

    @Test
    @DisplayName("Проверка создания сообщения регистрации")
    public void testGetContent() {
        when(telegramUser.getId()).thenReturn(12345L);
        when(telegramUser.getUserName()).thenReturn("testuser");
        when(message.getFrom()).thenReturn(telegramUser);
        when(message.getChatId()).thenReturn(12345L);
        when(update.getMessage()).thenReturn(message);

        SendMessage sendMessage = registerCommand.getContent(update);
        assertEquals("Вы веган?", sendMessage.getText(), "Сообщение должно быть 'Вы веган?'");
    }

    @Test
    @DisplayName("Проверка обработки callback-запросов для вопроса о веганах")
    public void testRegistrationCallbackHandlingVegan() {
        when(telegramUser.getId()).thenReturn(12345L);
        when(telegramUser.getUserName()).thenReturn("testuser");
        when(callbackQuery.getFrom()).thenReturn(telegramUser);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(callbackQuery.getData()).thenReturn("vegan_yes");
        when(message.getChatId()).thenReturn(12345L);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);

        when(databaseManager.getRegistrationStep(anyLong())).thenReturn(1);

        // Инициализация пользователя
        User user = new User(12345L, "testuser");
        registerCommand.registerUser(user);

        EditMessageContainer editMessageContainer = registerCommand.registration(update);
        assertEquals("Вы веган?", editMessageContainer.getEditMessageText(), "Сообщение должно быть 'Вы вегетарианец?'");
    }


    @Test
    @DisplayName("Проверка обработки callback-запросов для вопроса о вегетарианцах")
    public void testRegistrationCallbackHandlingVegetarian() {
        when(telegramUser.getId()).thenReturn(12345L);
        when(telegramUser.getUserName()).thenReturn("testuser");
        when(callbackQuery.getFrom()).thenReturn(telegramUser);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(callbackQuery.getData()).thenReturn("vegetarian_yes");
        when(message.getChatId()).thenReturn(12345L);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);

        when(databaseManager.getRegistrationStep(anyLong())).thenReturn(2);

        // Инициализация пользователя
        User user = new User(12345L, "testuser");
        registerCommand.registerUser(user);

        EditMessageContainer editMessageContainer = registerCommand.registration(update);
        assertEquals("Есть ли у вас аллергии?", editMessageContainer.getEditMessageText(), "Сообщение должно быть 'Есть ли у вас аллергии?'");
    }

    @Test
    @DisplayName("Проверка обработки callback-запросов для вопроса об аллергиях")
    public void testRegistrationCallbackHandlingAllergies() {
        when(telegramUser.getId()).thenReturn(12345L);
        when(telegramUser.getUserName()).thenReturn("testuser");
        when(callbackQuery.getFrom()).thenReturn(telegramUser);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(callbackQuery.getData()).thenReturn("allergies_yes");
        when(message.getChatId()).thenReturn(12345L);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);

        when(databaseManager.getRegistrationStep(anyLong())).thenReturn(3);

        // Инициализация пользователя
        User user = new User(12345L, "testuser");
        registerCommand.registerUser(user);

        EditMessageContainer editMessageContainer = registerCommand.registration(update);
        assertEquals("Какие у вас аллергии?", editMessageContainer.getEditMessageText(), "Сообщение должно быть 'Какие у вас аллергии?'");
    }

    @Test
    @DisplayName("Проверка создания Inline клавиатуры для вопроса о веганах")
    public void testFirstKeyboard() {
        InlineKeyboardMarkup inlineKeyboard = registerCommand.first_Keyboard();
        assertNotNull(inlineKeyboard, "InlineKeyboardMarkup не должен быть null");
        assertFalse(inlineKeyboard.getKeyboard().isEmpty(), "InlineKeyboard должен содержать кнопки");

        InlineKeyboardButton yesButton = inlineKeyboard.getKeyboard().get(0).get(0);
        InlineKeyboardButton noButton = inlineKeyboard.getKeyboard().get(0).get(1);

        assertEquals("Да", yesButton.getText(), "Текст кнопки должен быть 'Да'");
        assertEquals("vegan_yes", yesButton.getCallbackData(), "CallbackData кнопки должен быть 'vegan_yes'");
        assertEquals("Нет", noButton.getText(), "Текст кнопки должен быть 'Нет'");
        assertEquals("vegan_no", noButton.getCallbackData(), "CallbackData кнопки должен быть 'vegan_no'");
    }
}
