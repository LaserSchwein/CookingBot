package org.example.bot.commands;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.example.bot.TelegramBot;
import org.example.bot.api.TranslateService;
import org.example.bot.database.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.io.IOException;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HelpCommandTest {

    private HelpCommand helpCommand;
    private MockedStatic<TelegramBot> mockedStatic;
    private Update update;
    private Message message;
    private DatabaseManager databaseManager;
    private TranslateService translateService;

    @BeforeEach
    public void setUp() throws IOException {
        databaseManager = mock(DatabaseManager.class);
        OkHttpClient okHttpClient = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);

        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn("{\"translatedText\": \"Information about the bot\"}");

        translateService = new TranslateService(okHttpClient, databaseManager);
        helpCommand = new HelpCommand(databaseManager); // Инициализация с DatabaseManager
        LinkedHashMap<String, Command> commandMap = createCommandMap();
        mockedStatic = mockStatic(TelegramBot.class);
        mockedStatic.when(TelegramBot::getCommandMap).thenReturn(commandMap);
        update = mock(Update.class);
        message = mock(Message.class);
    }

    @AfterEach
    public void tearDown() {
        mockedStatic.close();
    }

    private LinkedHashMap<String, Command> createCommandMap() {
        LinkedHashMap<String, Command> commandMap = new LinkedHashMap<>();
        commandMap.put("/start", new StartCommand());
        commandMap.put("/info", new InfoCommand(databaseManager));
        commandMap.put("/authors", new AuthorsCommand(databaseManager));
        commandMap.put("/register", new RegisterCommand(databaseManager));
        commandMap.put("/recipes", new RecipesCommand(null, null)); // Adding RecipesCommand for completeness
        commandMap.put("/language", new LanguageCommand(databaseManager));
        return commandMap;
    }

    @Test
    @DisplayName("Проверка описания команды /help")
    public void testGetDescription() {
        String expectedDescription = "List of commands";
        String actualDescription = helpCommand.getDescription();
        assertEquals(expectedDescription, actualDescription, "Описание команды должно быть 'List of commands'");
    }

    @Test
    @DisplayName("Проверка текста помощи для команды /help")
    public void testGetContent() {
        // Устанавливаем mock данные
        long chatId = 123456L;
        when(message.getChatId()).thenReturn(chatId);
        when(update.getMessage()).thenReturn(message);
        when(databaseManager.getLanguage(chatId)).thenReturn("ru");

        // Create a spy of TranslateService
        TranslateService translateServiceSpy = spy(translateService);
        helpCommand = new HelpCommand(databaseManager); // Инициализация с DatabaseManager

        // Mock the translateService to return the expected descriptions
        doReturn("Information about the bot").when(translateServiceSpy).translateFromEnglish(anyString(), eq(chatId));
        doReturn("Authors").when(translateServiceSpy).translateFromEnglish(eq("Authors"), eq(chatId));
        doReturn("Registering an account in the bot").when(translateServiceSpy).translateFromEnglish(eq("Registering an account in the bot"), eq(chatId));
        doReturn("Select recipes based on your preferences.").when(translateServiceSpy).translateFromEnglish(eq("Select recipes based on your preferences."), eq(chatId));

        // Ожидаемый результат
        String expectedContent = """
                Доступные команды:
                /info - Информация о функционале бота
                /authors - Авторы
                /register - Регистрация аккаунта в боте
                /recipes - Выберите рецепты в соответствии с вашими предпочтениями.
                /language - Выберите один из предложенных языков:
                """;

        // Вызываем метод и получаем результат
        SendMessage sendMessage = helpCommand.getContent(update);
        assertNotNull(sendMessage, "Ответ сообщения не должен быть null");

        String actualContent = sendMessage.getText();
        assertNotNull(actualContent, "Текст сообщения не должен быть null");
        assertEquals(expectedContent, actualContent, "Контент помощи должен соответствовать ожидаемому");

        // Проверяем наличие клавиатуры
        assertNotNull(sendMessage.getReplyMarkup(), "Клавиатура должна быть установлена");
        assertInstanceOf(InlineKeyboardMarkup.class, sendMessage.getReplyMarkup(), "Клавиатура должна быть типа InlineKeyboardMarkup");
    }



    @Test
    @DisplayName("Проверка команды /help")
    public void testGetCommand() {
        String expectedCommand = "/help";
        String actualCommand = helpCommand.getCommand();
        assertEquals(expectedCommand, actualCommand, "Команда должна быть '/help'");
    }

    @Test
    @DisplayName("Проверка создания Inline клавиатуры")
    public void testCreateInlineCommandsKeyboard() {
        InlineKeyboardMarkup inlineKeyboard = helpCommand.createInlineCommandsKeyboard();
        assertNotNull(inlineKeyboard, "InlineKeyboardMarkup не должен быть null");
        assertFalse(inlineKeyboard.getKeyboard().isEmpty(), "InlineKeyboard должен содержать кнопки");

        // Проверяем, что кнопки соответствуют ожидаемым
        assertEquals(5, inlineKeyboard.getKeyboard().size(), "Количество кнопок должно быть 5");
        assertEquals("/info", inlineKeyboard.getKeyboard().get(0).get(0).getText(), "Первая кнопка должна быть '/info'");
        assertEquals("/authors", inlineKeyboard.getKeyboard().get(1).get(0).getText(), "Вторая кнопка должна быть '/authors'");
        assertEquals("/register", inlineKeyboard.getKeyboard().get(2).get(0).getText(), "Третья кнопка должна быть '/register'");
        assertEquals("/recipes", inlineKeyboard.getKeyboard().get(3).get(0).getText(), "Четвертая кнопка должна быть '/recipes'");
        assertEquals("/language", inlineKeyboard.getKeyboard().get(4).get(0).getText(), "Пятая кнопка должна быть '/language'");
    }

    @Test
    @DisplayName("Проверка создания Reply клавиатуры")
    public void testGetReplyKeyboard() {
        ReplyKeyboardMarkup replyKeyboard = helpCommand.getReplyKeyboard();
        assertNotNull(replyKeyboard, "ReplyKeyboardMarkup не должен быть null");
        assertFalse(replyKeyboard.getKeyboard().isEmpty(), "ReplyKeyboard должен содержать кнопки");

        // Проверяем, что кнопка соответствует ожидаемой
        assertEquals(1, replyKeyboard.getKeyboard().size(), "Количество строк должно быть 1");
        assertEquals(1, replyKeyboard.getKeyboard().get(0).size(), "Количество кнопок в строке должно быть 1");
        assertEquals("/help", replyKeyboard.getKeyboard().get(0).get(0).getText(), "Кнопка должна быть '/help'");
    }
}
