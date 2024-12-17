package org.example.bot.commands;

import org.example.bot.EditMessageContainer;
import org.example.bot.database.DatabaseManager;
import org.example.bot.database.User;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LanguageCommand implements Command {
    private final DatabaseManager databaseManager;

    public LanguageCommand(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public String getDescription() {
        return "Select one of the suggested languages:";
    }

    @Override
    public SendMessage getContent(Update update) {
        Long chatId;

        if (update.getMessage() == null) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }

        // Создаем сообщение с кнопками выбора языка
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Please select your language:");

        // Карта языков: отображаемое название -> префикс
        Map<String, String> languages = getLanguagesMap();

        // Создаем кнопки
        InlineKeyboardMarkup keyboardMarkup = getInlineKeyboardMarkup(languages);
        message.setReplyMarkup(keyboardMarkup);

        return message;
    }

    private static @NotNull InlineKeyboardMarkup getInlineKeyboardMarkup(Map<String, String> languages) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Map.Entry<String, String> entry : languages.entrySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(entry.getKey());
            button.setCallbackData("language:" + entry.getValue());
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    @Override
    public String getCommand() {
        return "/language";
    }

    public EditMessageContainer handleCallback(Update update) {
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long userId = update.getCallbackQuery().getFrom().getId();
            String userName = update.getCallbackQuery().getFrom().getUserName();

            if (!databaseManager.userExists(userId)) {
                User user = new User(userId, userName);
                registerUser(user);
                }

            if (callbackData.startsWith("language:")) {
                String selectedLanguagePrefix = callbackData.split(":")[1];

                // Сохраняем выбранный язык (префикс) в базу данных
                databaseManager.updateLanguage(userId, selectedLanguagePrefix);

                return new EditMessageContainer(update,
                        "Your language has been set to: " + selectedLanguagePrefix,
                        createEmptyKeyboard(),
                        databaseManager);
            } else if (callbackData.equals("/language")) {
                // Handle the /language command callback
                return new EditMessageContainer(update,
                        "Please select your language:",
                        getInlineKeyboardMarkup(getLanguagesMap()),
                        databaseManager);
            }
        }
        return null;
    }

    public void registerUser(User user) {
        databaseManager.addUser(user);
    }

    private InlineKeyboardMarkup createEmptyKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    private Map<String, String> getLanguagesMap() {
        Map<String, String> languages = new LinkedHashMap<>();
        languages.put("English", "en");
        languages.put("Русский", "ru");
        languages.put("Español", "es");
        languages.put("Français", "fr");
        languages.put("Deutsch", "de");
        return languages;
    }
}
