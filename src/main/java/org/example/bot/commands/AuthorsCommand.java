package org.example.bot.commands;

import okhttp3.OkHttpClient;
import org.example.bot.database.DatabaseManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.example.bot.api.TranslateService;

public class AuthorsCommand implements Command {
    private final TranslateService translateService;

    public AuthorsCommand(DatabaseManager databaseManager) {
        OkHttpClient okHttpClient = new OkHttpClient();
        this.translateService = new TranslateService(okHttpClient, databaseManager);
    }

    @Override
    public String getDescription() {return "Authors";}

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
        message.setText(translateService.translateFromEnglish("Authors: @sobol_eg, @ZAntoshkAZ, @polska_stronker", chatId));

        return message;
    }

    @Override
    public String getCommand() {
        return "/authors";
    }
}
