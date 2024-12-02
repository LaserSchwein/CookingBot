package org.example.bot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class AuthorsCommand implements Command {

    @Override
    public String getDescription() {
        return "Авторы";
    }

    @Override
    public SendMessage getContent(Update update) {
        SendMessage message = new SendMessage();
        if (update.getMessage() == null) {
            message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
        } else {
            message.setChatId(update.getMessage().getChatId().toString());
        }

        message.setText("Авторы: @sobol_eg, @ZAntoshkAZ, @polska_stronker");

        return message;
    }

    @Override
    public String getCommand() {
        return "/authors";
    }

    @Override
    public InlineKeyboardMarkup createHelpBackButtonKeyboard() {
        return Command.super.createHelpBackButtonKeyboard();
    }
}
