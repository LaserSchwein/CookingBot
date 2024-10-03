package org.example.bot.commands;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

public interface Command {
    String getDescription();
    String getContent();
    String getCommand();
    KeyboardButton getButton();
    default ReplyKeyboardMarkup getReplyKeyboard() {
        return null; // По умолчанию не возвращаем клавиатуру
    }
}
