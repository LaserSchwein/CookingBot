package org.example.bot.commands;

import okhttp3.OkHttpClient;
import org.example.bot.EditMessageContainer;
import org.example.bot.TelegramBot;
import org.example.bot.api.TranslateService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class RegisterCommandTest {

    private RegisterCommand registerCommand;
    private DatabaseManager databaseManager;
    private MockedStatic<TelegramBot> mockedStatic;
    private Update update;
    private Message message;
    private CallbackQuery callbackQuery;
    private org.telegram.telegrambots.meta.api.objects.User telegramUser;
    private StartCommand startCommand;
    private TranslateService translateService;

    @BeforeEach
    public void setUp() {
        databaseManager = mock(DatabaseManager.class);
        translateService = spy(new TranslateService(new OkHttpClient(), databaseManager));
        registerCommand = new RegisterCommand(databaseManager);
        startCommand = new StartCommand();
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
        String expectedDescription = "Registering an account in the bot";
        String actualDescription = registerCommand.getDescription();
        assertEquals(expectedDescription, actualDescription, "Описание команды должно быть 'Registering an account in the bot'");
    }

    @Test
    @DisplayName("Проверка команды /register")
    public void testGetCommand() {
        String expectedCommand = "/register";
        String actualCommand = registerCommand.getCommand();
        assertEquals(expectedCommand, actualCommand, "Команда должна быть '/register'");
    }

    @Test
    @DisplayName("Проверка создания сообщения регистрации при использовании /start")
    public void testGetContentWithStartCommand() {
        long chatId = 12345L;
        when(telegramUser.getId()).thenReturn(12345L);
        when(databaseManager.getLanguage(chatId)).thenReturn("ru");
        when(telegramUser.getUserName()).thenReturn("testuser");
        when(message.getFrom()).thenReturn(telegramUser);
        when(message.getChatId()).thenReturn(12345L);
        when(update.getMessage()).thenReturn(message);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage().hasText()).thenReturn(true);

        // Use the start command to initiate registration
        SendMessage sendMessage = startCommand.getContent(update);
        assertEquals("Welcome to the recipe bot! Use /help for a list of commands.", sendMessage.getText(), "Сообщение должно быть 'Welcome to the recipe bot! Use /help for a list of commands.'");

        // Mock the translation service to return the expected translation
        doReturn("Вы веган?").when(translateService).translateFromEnglish(anyString(), eq(chatId));

        // Now check the registration message
        sendMessage = registerCommand.getContent(update);
        assertEquals("Вы веган?", sendMessage.getText(), "Сообщение должно быть 'Вы веган?'");
    }

    @Test
    @DisplayName("Проверка обработки callback-запросов для вопроса о веганах")
    public void testRegistrationCallbackHandlingVegan() {
        long chatId = 12345L;
        when(telegramUser.getId()).thenReturn(chatId);
        when(telegramUser.getUserName()).thenReturn("testuser");
        when(callbackQuery.getFrom()).thenReturn(telegramUser);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(callbackQuery.getData()).thenReturn("vegan_yes");
        when(message.getChatId()).thenReturn(chatId);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(databaseManager.getLanguage(chatId)).thenReturn("ru");

        when(databaseManager.getRegistrationStep(anyLong())).thenReturn(1);

        doReturn("Вы вегетарианец?").when(translateService).translateFromEnglish(anyString(), eq(chatId));

        // Initialize user
        registerCommand.user = new User(chatId, "");

        EditMessageContainer editMessageContainer = registerCommand.registration(update);
        assertEquals("12. Вы вегетарианец?", editMessageContainer.getEditMessageText(), "Сообщение должно быть '12. Вы вегетарианец?'");
    }

    @Test
    @DisplayName("Проверка обработки callback-запросов для вопроса о вегетарианцах")
    public void testRegistrationCallbackHandlingVegetarian() {
        long chatId = 12345L;
        when(telegramUser.getId()).thenReturn(chatId);
        when(telegramUser.getUserName()).thenReturn("testuser");
        when(callbackQuery.getFrom()).thenReturn(telegramUser);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(callbackQuery.getData()).thenReturn("vegetarian_yes");
        when(message.getChatId()).thenReturn(chatId);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(databaseManager.getLanguage(chatId)).thenReturn("ru");

        when(databaseManager.getRegistrationStep(anyLong())).thenReturn(2);

        doReturn("У вас есть аллергии?").when(translateService).translateFromEnglish(anyString(), eq(chatId));

        // Initialize user
        registerCommand.user = new User(chatId, "");

        EditMessageContainer editMessageContainer = registerCommand.registration(update);
        assertEquals("У вас есть аллергия на что-нибудь?", editMessageContainer.getEditMessageText(), "Сообщение должно быть 'У вас есть аллергии?'");
    }

    @Test
    @DisplayName("Проверка обработки callback-запросов для вопроса об аллергиях")
    public void testRegistrationCallbackHandlingAllergies() {
        long chatId = 12345L;
        when(telegramUser.getId()).thenReturn(chatId);
        when(telegramUser.getUserName()).thenReturn("testuser");
        when(callbackQuery.getFrom()).thenReturn(telegramUser);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(callbackQuery.getData()).thenReturn("allergies_yes");
        when(message.getChatId()).thenReturn(chatId);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(databaseManager.hasAllergies(anyLong())).thenReturn(true);
        when(databaseManager.getLanguage(chatId)).thenReturn("ru");

        when(databaseManager.getRegistrationStep(update.getCallbackQuery().getMessage().getChatId())).thenReturn(3);

        doReturn("Какие у вас аллергии?").when(translateService).translateFromEnglish(anyString(), eq(chatId));

        // Initialize user
        registerCommand.user = new User(chatId, "");

        EditMessageContainer editMessageContainer = registerCommand.registration(update);
        assertEquals("Какая у вас аллергия?", editMessageContainer.getEditMessageText(), "Сообщение должно быть 'Какие у вас аллергии?'");
    }

    @Test
    @DisplayName("Проверка создания Inline клавиатуры для вопроса о веганах")
    public void testFirstKeyboard() {
        InlineKeyboardMarkup inlineKeyboard = registerCommand.first_Keyboard();
        assertNotNull(inlineKeyboard, "InlineKeyboardMarkup не должен быть null");
        assertFalse(inlineKeyboard.getKeyboard().isEmpty(), "InlineKeyboard должен содержать кнопки");

        InlineKeyboardButton yesButton = inlineKeyboard.getKeyboard().get(0).get(0);
        InlineKeyboardButton noButton = inlineKeyboard.getKeyboard().get(0).get(1);

        assertEquals("✅", yesButton.getText(), "Текст кнопки должен быть '✅'");
        assertEquals("vegan_yes", yesButton.getCallbackData(), "CallbackData кнопки должен быть 'vegan_yes'");
        assertEquals("❎", noButton.getText(), "Текст кнопки должен быть '❎'");
        assertEquals("vegan_no", noButton.getCallbackData(), "CallbackData кнопки должен быть 'vegan_no'");
    }
}
