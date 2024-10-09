package org.example.bot;

import org.example.bot.commands.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.LinkedHashMap;

public class TelegramBot extends TelegramLongPollingBot {

    private static final LinkedHashMap<String, Command> commands = new LinkedHashMap<>();

    public TelegramBot() {
        // Регистрация команд
        commands.put("/start", new StartCommand());
        commands.put("/help", new HelpCommand());
        commands.put("/info", new InfoCommand());
        commands.put("/authors", new AuthorsCommand());
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
            String text = update.getMessage().getText();

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());

            // Проверка наличия команды в хеш-таблице
            Command command = commands.get(text);
            if (command != null) {
                sendMessage.setText(command.getContent());

                // Если команда /help, добавляем inline-клавиатуру и отправляем одно сообщение
                if (command instanceof HelpCommand) {
                    sendMessage.setReplyMarkup(((HelpCommand) command).getInlineKeyboard());
                }
            } else {
                sendMessage.setText("Извините, я не понимаю эту команду. Напишите /help для получения списка команд.");
            }

            try {
                this.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }


    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();

        // Если нажали "Назад", возвращаемся к команде /help
        if ("/help".equals(data)) {
            Command helpCommand = commands.get("/help");

            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(callbackQuery.getMessage().getChatId().toString());
            editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
            editMessageText.setText(helpCommand.getContent());
            editMessageText.setReplyMarkup(((HelpCommand) helpCommand).getInlineKeyboard());

            try {
                this.execute(editMessageText);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            Command command = commands.get(data);

            if (command != null) {
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(callbackQuery.getMessage().getChatId().toString());
                editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
                editMessageText.setText(command.getContent());

                EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
                editMarkup.setChatId(callbackQuery.getMessage().getChatId().toString());
                editMarkup.setMessageId(callbackQuery.getMessage().getMessageId());
                editMarkup.setReplyMarkup(command.getInlineKeyboard());  // Добавляем inline-клавиатуру с кнопкой "Назад"

                try {
                    this.execute(editMessageText);
                    this.execute(editMarkup);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(callbackQuery.getMessage().getChatId().toString());
                sendMessage.setText("Извините, я не понимаю эту команду. Напишите /help для получения списка команд.");

                try {
                    this.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
