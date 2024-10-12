package org.example.bot;

import org.example.bot.commands.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.LinkedHashMap;

public class TelegramBot extends TelegramLongPollingBot {

    private static final LinkedHashMap<String, Command> commands = new LinkedHashMap<>();

    public TelegramBot() {
        // Регистрация команд
        commands.put("/start", new StartCommand());
        commands.put("/help", new HelpCommand());
        commands.put("/info", new InfoCommand());
        commands.put("/authors", new AuthorsCommand());
        commands.put("/register", new RegisterCommand());
    }

    public static LinkedHashMap<String, Command> getCommandMap() {
        return commands;
    }

    @Override
    public String getBotToken() {
        return System.getenv("TELEGRAM_BOT_TOKEN");
    }

    @Override
    public String getBotUsername() {
        return "GrandmaIsRecipes_bot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleCommand(update.getMessage().getChatId().toString(), update.getMessage().getText());
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleCommand(String chatId, String commandText) {
        Command command = commands.getOrDefault(commandText, null);
        if (command != null) {
            sendMessageWithKeyboard(chatId, command.getContent(), command);
        } else {
            sendSimpleMessage(chatId, "Извините, я не понимаю эту команду. Напишите /help для получения списка команд.");
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Command command = commands.getOrDefault(data, null);

        if (command != null) {
            editMessageWithKeyboard(
                    callbackQuery.getMessage().getChatId().toString(),
                    callbackQuery.getMessage().getMessageId(),
                    command.getContent(),
                    command
            );
        } else {
            sendSimpleMessage(callbackQuery.getMessage().getChatId().toString(),
                    "Извините, я не понимаю эту команду. Напишите /help для получения списка команд.");
        }
    }

    private void sendSimpleMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageWithKeyboard(String chatId, String text, Command command) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(command instanceof HelpCommand
                ? ((HelpCommand) command).createInlineCommandsKeyboard()
                : command.createHelpBackButtonKeyboard());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void editMessageWithKeyboard(String chatId, Integer messageId, String text, Command command) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        editMessageText.setText(text);

        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(chatId);
        editMarkup.setMessageId(messageId);
        editMarkup.setReplyMarkup(command instanceof HelpCommand
                ? ((HelpCommand) command).createInlineCommandsKeyboard()
                : command.createHelpBackButtonKeyboard());

        try {
            execute(editMessageText);
            execute(editMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
