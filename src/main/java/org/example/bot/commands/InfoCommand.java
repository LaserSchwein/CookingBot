package org.example.bot.commands;

import okhttp3.OkHttpClient;
import org.example.bot.api.TranslateService;
import org.example.bot.database.DatabaseManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class InfoCommand implements Command {
    private final TranslateService translateService;

    public InfoCommand(DatabaseManager databaseManager) {
        OkHttpClient okHttpClient = new OkHttpClient();
        this.translateService = new TranslateService(okHttpClient, databaseManager);
    }

    @Override
    public String getDescription() {
        return "Information about the bot functionality";
    }

    @Override
    public SendMessage getContent(Update update){
        SendMessage message = new SendMessage();
        Long chatId;

        if (update.getMessage() == null) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }

        message.setChatId(chatId);
        message.setText(translateService.translateFromEnglish("This is a universal bot that will help you choose a dish, write a recipe from the ingredients you have, and much more.", chatId));

        return message;
    }

    @Override
    public String getCommand() {
        return "/info";
    }
}
