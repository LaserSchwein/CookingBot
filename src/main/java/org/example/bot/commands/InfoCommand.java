package org.example.bot.commands;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class InfoCommand implements Command {

    @Override
    public String getDescription() {
        return "Информация о боте";
    }

    @Override
    public String getContent() {
        return "Это универсальный бот, который поможет вам выбрать блюдо, написать рецепт из имеющихся у вас ингредиентов и многое другое.";
    }

    @Override
    public String getCommand() {
        return "/info";
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText("⬅️ Назад");
        backButton.setCallbackData("/help");
        keyboard.add(List.of(backButton));

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }
}
