package org.example.bot.commands;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class RegisterCommand implements Command {
    @Override
    public String getDescription() {
        return "Регистрация аккаунта в боте";
    }

    @Override
    public String getContent() {
        return "зарегестрируйтесь";
    }

    @Override
    public String getCommand() {
        return "/register";
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboard() {
        return Command.super.getInlineKeyboard();
    }
}
