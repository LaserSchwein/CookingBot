package org.example.bot;

import org.example.bot.commands.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
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

                // Добавляем только reply keyboard для команды /help
                if (command instanceof HelpCommand) {
                    sendMessage.setReplyMarkup(((HelpCommand) command).getReplyKeyboard());
                }
            } else {
                sendMessage.setText("Извините, я не понимаю эту команду. Напишите /help для получения списка команд.");
            }

            try {
                this.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            // Если команда /help, отправляем inline keyboard отдельным сообщением
            if ("/help".equals(text)) {
                HelpCommand helpCommand = (HelpCommand) command;
                SendMessage inlineMessage = new SendMessage();
                inlineMessage.setChatId(update.getMessage().getChatId().toString());
                inlineMessage.setText("Выберите команду:");

                // Логирование времени выполнения метода getInlineKeyboard
                long startTimeKeyboard = System.currentTimeMillis();
                inlineMessage.setReplyMarkup(helpCommand.getInlineKeyboard());
                long endTimeKeyboard = System.currentTimeMillis();
                System.out.println("Time taken to get inline keyboard: " + (endTimeKeyboard - startTimeKeyboard) + " ms");

                try {
                    this.execute(inlineMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(callbackQuery.getMessage().getChatId().toString());

        Command command = commands.get(data);
        if (command != null) {
            sendMessage.setText(command.getContent());
        } else {
            sendMessage.setText("Извините, я не понимаю эту команду. Напишите /help для получения списка команд.");
        }
        try {
            this.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
