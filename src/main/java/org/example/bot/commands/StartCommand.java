package org.example.bot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class StartCommand implements Command {

    @Override
    public String getDescription() {
        return "Запуск бота";
    }

    @Override
    public SendMessage getContent(Update update) {
        SendMessage message = new SendMessage();
        if (update.getMessage() == null) {
            message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
        } else {
            message.setChatId(update.getMessage().getChatId().toString());
        }
        message.setText("Welcome to the recipe bot! Use /help for a list of commands.");

        return message;
    }

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public InlineKeyboardMarkup createHelpBackButtonKeyboard() {
        return Command.super.createHelpBackButtonKeyboard();
    }
}
