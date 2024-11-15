package org.example.bot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class InfoCommand implements Command {

    @Override
    public String getDescription() {
        return "Информация о боте";
    }

    @Override
    public SendMessage getContent(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("Это универсальный бот, который поможет вам выбрать блюдо, написать рецепт из имеющихся у вас ингредиентов и многое другое.");

        return message;
    }

    @Override
    public String getCommand() {
        return "/info";
    }

    @Override
    public InlineKeyboardMarkup createHelpBackButtonKeyboard() {
        return Command.super.createHelpBackButtonKeyboard();
    }
}
