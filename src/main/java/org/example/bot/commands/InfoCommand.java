package org.example.bot.commands;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

public class InfoCommand implements Command {
    @Override
    public String getDescription() {
        return "Информация о боте";
    }

    @Override
    public String getContent() {
        return "Это универсальный бот, который поможет вам выбрать блюдо, написать рецепт из имеющихся у вас ингредиентов и многое другое";
    }

    @Override
    public String getCommand() {
        return "/info";
    }

    @Override
    public KeyboardButton getButton() {
        return new KeyboardButton(getCommand());
    }
}