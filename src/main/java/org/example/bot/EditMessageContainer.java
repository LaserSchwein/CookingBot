package org.example.bot;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class EditMessageContainer {
    private EditMessageText editMessageText;
    private EditMessageReplyMarkup editMessageReplyMarkup;

    public EditMessageContainer(Update update, String text, InlineKeyboardMarkup markup) {
        this.editMessageText = crateEditMessageText(update, text);
        this.editMessageReplyMarkup = crateEditMessageReplyMarkup(update, markup);
    }

    public String getEditMessageText() {
        return editMessageText.getText();
    }

    public InlineKeyboardMarkup getEditMessageReplyMarkup() {
        return editMessageReplyMarkup.getReplyMarkup();
    }

    public EditMessageText crateEditMessageText(Update update, String question) {
        EditMessageText editMessageText = new EditMessageText();

        if (update.hasMessage() && update.getMessage().hasText()){
            editMessageText.setChatId(update.getMessage().getChatId().toString());
            editMessageText.setMessageId(update.getMessage().getMessageId());
        } else {
            editMessageText.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
            editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        }

        editMessageText.setText(question);

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
