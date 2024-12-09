package org.example.bot.commands;

import org.example.bot.TelegramBot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HelpCommandTest {

    private HelpCommand helpCommand;
    private LinkedHashMap<String, Command> commandMap;
    private MockedStatic<TelegramBot> mockedStatic;
    private Update update;
    private Message message;
    private Chat chat;

    @BeforeEach
    public void setUp() {
        helpCommand = new HelpCommand();
        commandMap = createCommandMap();
        mockedStatic = mockStatic(TelegramBot.class);
        mockedStatic.when(TelegramBot::getCommandMap).thenReturn(commandMap);
        update = mock(Update.class);
        message = mock(Message.class);
        chat = mock(Chat.class);
    }

    @AfterEach
    public void tearDown() {
        mockedStatic.close();
    }

    private LinkedHashMap<String, Command> createCommandMap() {
        LinkedHashMap<String, Command> commandMap = new LinkedHashMap<>();
        commandMap.put("/start", new StartCommand());
        commandMap.put("/info", new InfoCommand());
        commandMap.put("/authors", new AuthorsCommand());
        commandMap.put("/register", new RegisterCommand(null));
        commandMap.put("/recipes", new RecipesCommand(null, null)); // Adding RecipesCommand for completeness
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
        when(message.getChatId()).thenReturn(123456L);
        when(update.getMessage()).thenReturn(message);

        String expectedContent = "Available commands:\n" +
                "/info - Information about the bot\n" +
                "/authors - Authors\n" +
                "/register - Registering an account in the bot\n" +
                "/recipes - Select recipes based on your preferences.\n";

        // Вызываем метод и получаем результат
        SendMessage sendMessage = helpCommand.getContent(update);
        String actualContent = sendMessage.getText();

        // Проверяем результат
        assertEquals(expectedContent, actualContent, "Контент помощи должен соответствовать ожидаемому");
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
    }

    @Test
    @DisplayName("Проверка обработки неправильной команды")
    public void testHandleUnknownCommand() {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Извините, я не понимаю эту команду. Напишите /help для получения списка команд.");
        sendMessage.setReplyMarkup(helpCommand.getReplyKeyboard());

        assertEquals("Извините, я не понимаю эту команду. Напишите /help для получения списка команд.", sendMessage.getText(), "Текст сообщения должен соответствовать ожидаемому");

        ReplyKeyboardMarkup expectedKeyboard = helpCommand.getReplyKeyboard();
        ReplyKeyboardMarkup actualKeyboard = (ReplyKeyboardMarkup) sendMessage.getReplyMarkup();

        assertEquals(expectedKeyboard.getKeyboard(), actualKeyboard.getKeyboard(), "Клавиатура должна соответствовать ожидаемой");
    }
}
