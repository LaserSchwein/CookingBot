package org.example.bot.commands;

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
