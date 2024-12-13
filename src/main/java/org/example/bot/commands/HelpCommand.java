package org.example.bot.commands;

import okhttp3.OkHttpClient;
import org.example.bot.TelegramBot;
import org.example.bot.api.TranslateService;
import org.example.bot.database.DatabaseManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HelpCommand implements Command {
    private InlineKeyboardMarkup cachedInlineKeyboard; // Кэш для inline клавиатуры
    private final TranslateService translateService;

    public HelpCommand(DatabaseManager databaseManager) {
        OkHttpClient okHttpClient = new OkHttpClient();
        this.translateService = new TranslateService(okHttpClient, databaseManager);
    }
    @Override
    public String getDescription() {
        return "List of commands";
    }

    @Override
    public SendMessage getContent(Update update){
        SendMessage message = new SendMessage();
        Long chatId;

        if (update.getMessage() == null) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }

        message.setChatId(chatId);

        StringBuilder helpMessage = new StringBuilder("Available commands:\n");
        for (Map.Entry<String, Command> entry : TelegramBot.getCommandMap().entrySet()) {
            if (!entry.getKey().equals("/help") && !entry.getKey().equals("/start")) {
                helpMessage.append(entry.getKey()).append(" - ").append(translateService.translateFromEnglish(entry.getValue().getDescription(), chatId)).append("\n");
            }
        }

        message.setText(helpMessage.toString());
        message.setReplyMarkup(createInlineCommandsKeyboard());

        return message;
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

    public InlineKeyboardMarkup createInlineCommandsKeyboard() {
        if (cachedInlineKeyboard == null) {
            cachedInlineKeyboard = createInlineKeyboard(); // Создаем клавиатуру только один раз
        }

        return cachedInlineKeyboard;
    }

    private InlineKeyboardMarkup createInlineKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Map.Entry<String, Command> entry : TelegramBot.getCommandMap().entrySet()) {
            Set<String> excludedKeys = Set.of("/help", "/start");
            if (!excludedKeys.contains(entry.getKey())) {
                keyboard.add(List.of(createPut(entry.getValue().getCommand(), entry.getKey())));
            }
        }

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardButton createPut(String text, String data) {
        InlineKeyboardButton put = new InlineKeyboardButton();
        put.setText(text);
        put.setCallbackData(data);
        return put;
    }
}
