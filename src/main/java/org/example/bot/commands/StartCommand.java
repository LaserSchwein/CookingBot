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
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("Добро пожаловать в бот с рецептами! Используйте /help для получения списка команд.");

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
