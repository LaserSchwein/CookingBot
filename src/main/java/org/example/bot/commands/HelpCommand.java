package org.example.bot.commands;

import org.example.bot.TelegramBot;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HelpCommand implements Command {

    private InlineKeyboardMarkup cachedInlineKeyboard; // Кэш для inline клавиатуры

    @Override
    public String getDescription() {
        return "Список команд";
    }

    @Override
    public String getContent() {
        StringBuilder helpMessage = new StringBuilder("Доступные команды:\n");
        for (Map.Entry<String, Command> entry : TelegramBot.getCommandMap().entrySet()) {
            if (!entry.getKey().equals("/help")) {
                helpMessage.append(entry.getKey()).append(" - ").append(entry.getValue().getDescription()).append("\n");
            }
        }
        return helpMessage.toString();
    }

    @Override
    public String getCommand() {
        return "/help";
    }

    public KeyboardButton getButton() {
        return new KeyboardButton(getCommand());
    }

    public ReplyKeyboardMarkup getReplyKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(getButton());
        keyboard.add(row);

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup getInlineKeyboard() {
        if (cachedInlineKeyboard == null) {
            cachedInlineKeyboard = createInlineKeyboard(); // Создаем клавиатуру только один раз
        }
        return cachedInlineKeyboard;
    }

    private InlineKeyboardMarkup createInlineKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Map.Entry<String, Command> entry : TelegramBot.getCommandMap().entrySet()) {
            if (!entry.getKey().equals("/help")) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(entry.getValue().getCommand());
                button.setCallbackData(entry.getKey());
                keyboard.add(List.of(button));
            }
        }

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }
}
