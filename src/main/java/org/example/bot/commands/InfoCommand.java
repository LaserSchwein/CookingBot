package org.example.bot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class InfoCommand implements Command {
    @Override
    public SendMessage execute(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId().toString());
        sendMessage.setText("Это универсалльный бот, который поможет вам выбрать блюдо, написать рецепт из имеющихся у сам ингредиентов и многое другое");
        return sendMessage;
    }
}
