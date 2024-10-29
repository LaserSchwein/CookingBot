package org.example.bot.commands;

import org.example.bot.DatabaseManager;
import org.example.bot.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class RegisterCommand implements Command {
    private final DatabaseManager databaseManager = new DatabaseManager();
    public void registerUser(User user) {
        databaseManager.DatabaseHandler();
        databaseManager.addUser(user);
    }

    @Override
    public String getDescription() {
        return "Регистрация аккаунта в боте";
    }

    @Override
    public String getContent() {
        return "Вы зарегистрировались";
    }

    @Override
    public String getCommand() {
        return "/register";
    }

    @Override
    public InlineKeyboardMarkup createHelpBackButtonKeyboard() {
        return Command.super.createHelpBackButtonKeyboard();
    }
}
