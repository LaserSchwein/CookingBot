package org.example.bot.commands;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

public class AuthorsCommand implements Command {
    @Override
    public String getDescription() {
        return "Авторы";
    }

    @Override
    public String getContent() {
        return "Авторы: @sobol_eg, @ZAntoshkAZ, @polska_stronker";
    }

    @Override
    public String getCommand() {
        return "/authors";
    }

    @Override
    public KeyboardButton getButton() {
        return new KeyboardButton(getCommand());
    }
}