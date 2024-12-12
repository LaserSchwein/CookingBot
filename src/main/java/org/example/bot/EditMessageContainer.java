package org.example.bot;

import okhttp3.OkHttpClient;
import org.example.bot.api.TranslateService;
import org.example.bot.database.DatabaseManager;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class EditMessageContainer {
    private final EditMessageText editMessageText;
    private final EditMessageReplyMarkup editMessageReplyMarkup;
    private final TranslateService translateService;

    public EditMessageContainer(Update update, String text, InlineKeyboardMarkup markup, DatabaseManager databaseManager) {
        OkHttpClient okHttpClient = new OkHttpClient();
        this.translateService = new TranslateService(okHttpClient, databaseManager);
        this.editMessageText = crateEditMessageText(update, text);
        this.editMessageReplyMarkup = crateEditMessageReplyMarkup(update, markup);
    }

    public String getEditMessageText() {
        return editMessageText.getText();
    }

    public InlineKeyboardMarkup getEditMessageReplyMarkup() {
        return editMessageReplyMarkup.getReplyMarkup();
    }

    public EditMessageText crateEditMessageText(Update update, String text) {
        EditMessageText editMessageText = new EditMessageText();

        Long chatId;

        if (update.getMessage() == null) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        } else {
            chatId = update.getMessage().getChatId();
            editMessageText.setMessageId(update.getMessage().getMessageId());
        }

        editMessageText.setChatId(chatId);
        editMessageText.setText(translateService.translateFromEnglish(text, chatId));

        return editMessageText;
    }

    public EditMessageReplyMarkup crateEditMessageReplyMarkup(Update update, InlineKeyboardMarkup markup) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();

        if (update.hasMessage() && update.getMessage().hasText()){
            editMessageReplyMarkup.setChatId(update.getMessage().getChatId().toString());
            editMessageReplyMarkup.setMessageId(update.getMessage().getMessageId());
        } else {
            editMessageReplyMarkup.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
            editMessageReplyMarkup.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        }

        editMessageReplyMarkup.setReplyMarkup(markup);

        return editMessageReplyMarkup;
    }
}
