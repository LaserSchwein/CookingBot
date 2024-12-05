package org.example.bot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class InfoCommand implements Command {

    @Override
    public String getDescription() {
        return "Information about the bot";
    }

    @Override
    public SendMessage getContent(Update update) {
        SendMessage message = new SendMessage();
        if (update.getMessage() == null) {
            message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
        } else {
            message.setChatId(update.getMessage().getChatId().toString());
        }
        message.setText("This is a universal bot that will help you choose a dish, write a recipe from the ingredients you have, and much more.");

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
