package org.example;

import org.example.bot.TelegramBot;
import org.example.bot.database.DatabaseManager;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        final DatabaseManager databaseManager = new DatabaseManager();

        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new TelegramBot(databaseManager));
            logger.info("Telegram bot registered successfully.");
        } catch (TelegramApiException e) {
            logger.log(Level.SEVERE, "Error registering Telegram bot", e);
        }
    }
}
