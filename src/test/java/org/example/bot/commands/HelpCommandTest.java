package org.example.bot.commands;

import org.example.bot.TelegramBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class HelpCommandTest {

    private HelpCommand helpCommand;

    @BeforeEach
    public void setUp() {
        helpCommand = new HelpCommand();
    }

    @Test
    @DisplayName("Проверка описания команды /help")
    public void testGetDescription() {
        String expectedDescription = "Список команд";
        String actualDescription = helpCommand.getDescription();
        assertEquals(expectedDescription, actualDescription, "Описание команды должно быть 'Список команд'");
    }

    @Test
    @DisplayName("Проверка текста помощи для команды /help")
    public void testGetContent() {
        String expectedContent = "Доступные команды:\n" +
                "/start - Запуск бота\n" +
                "/info - Информация о боте\n" +
                "/authors - Авторы бота\n" +
                "/register - Регистрация\n";

        // Мокаем объект Map
        Map<String, Command> commandMap = mock(Map.class);
        Mockito.when(commandMap.entrySet()).thenReturn(Map.of(
                "/start", new StartCommand(),
                "/info", new InfoCommand(),
                "/authors", new AuthorsCommand(),
                "/register", new RegisterCommand()
        ).entrySet());

        // Мокаем вызов TelegramBot.getCommandMap()
        Mockito.mockStatic(TelegramBot.class).when(TelegramBot::getCommandMap).thenReturn(commandMap);

        String actualContent = helpCommand.getContent();
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
}
