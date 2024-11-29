package org.example;

import org.example.bot.TelegramBot;
import org.example.bot.database.DatabaseManager;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        final DatabaseManager databaseManager = new DatabaseManager();

        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new TelegramBot(databaseManager));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
