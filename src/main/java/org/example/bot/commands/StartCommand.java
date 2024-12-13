package org.example.bot.commands;

import okhttp3.OkHttpClient;
import org.example.bot.api.TranslateService;
import org.example.bot.database.DatabaseManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class StartCommand implements Command {

    @Override
    public String getDescription() {
        return "Start bot";
    }

    @Override
    public SendMessage getContent(Update update) {
        SendMessage message = new SendMessage();
        Long chatId;

        if (update.getMessage() == null) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }

        message.setChatId(chatId);
        message.setText("Welcome to the recipe bot! Use /help for a list of commands.");

        return message;
    }

    @Override
    public String getCommand() {
        return "/start";
    }
}
