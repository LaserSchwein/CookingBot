package org.example.bot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class StartCommand implements Command {

    @Override
    public String getDescription() {
        return "Начало работы бота";
    }

    @Override
    public String getContent() {
        return "Добро пожаловать! Введите /help для начала работы с ботом.";
    }

    @Override
    public String getCommand() {
        return "/start";
    }
}
