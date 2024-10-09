package org.example.bot.commands;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface Command {
    String getDescription();
    String getContent();
    String getCommand();
    InlineKeyboardMarkup getInlineKeyboard();
}
