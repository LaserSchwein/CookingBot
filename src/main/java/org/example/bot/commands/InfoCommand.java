package org.example.bot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

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
}
