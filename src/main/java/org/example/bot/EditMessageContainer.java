package org.example.bot;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class EditMessageContainer {
    private EditMessageText editMessageText;
    private EditMessageReplyMarkup editMessageReplyMarkup;

    public EditMessageContainer(EditMessageText editMessageText, EditMessageReplyMarkup editMessageReplyMarkup) {
        this.editMessageText = editMessageText;
        this.editMessageReplyMarkup = editMessageReplyMarkup;
    }

    public String getEditMessageText() {
        return editMessageText.getText();
    }

    public InlineKeyboardMarkup getEditMessageReplyMarkup() {
        return editMessageReplyMarkup.getReplyMarkup();
    }
}
