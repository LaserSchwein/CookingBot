package org.example.bot.commands;

import org.example.bot.DatabaseManager;
import org.example.bot.User;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class RegisterCommand implements Command {
    private final DatabaseManager databaseManager = new DatabaseManager();

    public void registerUser(User user) {
        databaseManager.addUser(user);
    }

    @Override
    public String getDescription() {
        return "Регистрация аккаунта в боте";
    }

    @Override
    public String getContent(Update update){

    long userId;
    String userName;

    if (update.hasMessage()&&update.getMessage().hasText()) {
        userId = update.getMessage().getFrom().getId();
        userName = update.getMessage().getFrom().getUserName();
    } else if (update.hasCallbackQuery()) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        userId = callbackQuery.getFrom().getId();
        userName = callbackQuery.getFrom().getUserName();
    } else {
        return "Ошибка регистрации: некорректный запрос.";
    }

    User user = new User(userId, userName);

    registerUser(user);
        return"Вы зарегистрировались";
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
