package org.example.bot.commands;

import okhttp3.OkHttpClient;
import org.example.bot.EditMessageContainer;
import org.example.bot.api.TranslateService;
import org.example.bot.database.DatabaseManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class EditProfileCommand implements Command {
    private final DatabaseManager databaseManager;
    private final TranslateService translateService;
    private int step = 0;

    public EditProfileCommand(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        OkHttpClient okHttpClient = new OkHttpClient();
        this.translateService = new TranslateService(okHttpClient, databaseManager);
    }

    @Override
    public String getDescription() {
        return "Edit your profile information";
    }

    @Override
    public String getCommand() {
        return "/editprofile";
    }

    @Override
    public SendMessage getContent(Update update) {
        return askEditOption(update);
    }

    private SendMessage askEditOption(Update update) {
        InlineKeyboardMarkup markup = createEditOptionsKeyboard();

        SendMessage message = new SendMessage();
        long chatId;

        if (update.getMessage() == null) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }
        message.setChatId(chatId);
        message.setText(translateService.translateFromEnglish("What would you like to edit?", chatId));
        message.setReplyMarkup(markup);

        step = 1;
        databaseManager.updateRegistrationStep(chatId, step);
        return message;
    }

    private InlineKeyboardMarkup createEditOptionsKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createPut(" Vegan", "edit_vegan"));
        row.add(createPut(" Vegetarian", "edit_vegetarian"));
        row.add(createPut(" Allergies", "edit_allergies"));
        rows.add(row);
        markup.setKeyboard(rows);
        return markup;
    }

    private EditMessageContainer askVeganQuestion(Update update) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createPut("✅", "edit_vegan_yes"));
        row.add(createPut("❎", "edit_vegan_no"));
        rows.add(row);
        markup.setKeyboard(rows);

        step = 2;
        databaseManager.updateRegistrationStep(update.getCallbackQuery().getMessage().getChatId(), step);
        return new EditMessageContainer(update,
                "Are you vegan?",
                markup,
                databaseManager);
    }

    private EditMessageContainer askVegetarianQuestion(Update update) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createPut("✅", "edit_vegetarian_yes"));
        row.add(createPut("❎", "edit_vegetarian_no"));
        rows.add(row);
        markup.setKeyboard(rows);

        step = 4;
        databaseManager.updateRegistrationStep(update.getCallbackQuery().getMessage().getChatId(), step);
        return new EditMessageContainer(update,
                "Are you a vegetarian?",
                markup,
                databaseManager);
    }

    private EditMessageContainer askAllergiesQuestion(Update update) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createPut("✅", "edit_allergies_yes"));
        row.add(createPut("❎", "edit_allergies_no"));
        rows.add(row);
        markup.setKeyboard(rows);

        step = 6;
        databaseManager.updateRegistrationStep(update.getCallbackQuery().getMessage().getChatId(), step);
        return new EditMessageContainer(update,
                "Do you have any allergies?",
                markup,
                databaseManager);
    }

    private EditMessageContainer askAllergiesDetailsQuestion(Update update) {
        step = 7;
        databaseManager.updateRegistrationStep(update.getCallbackQuery().getMessage().getChatId(), step);
        return new EditMessageContainer(update,
                "What allergies do you have?",
                createEmptyKeyboard(),
                databaseManager);
    }

    public EditMessageContainer handleCallbackQuery(CallbackQuery callbackQuery, Update update, int step) {
        String data = callbackQuery.getData();

        if (step == 0) {
            SendMessage sendMessage = getContent(update);
            return new EditMessageContainer(update,
                    sendMessage.getText(),
                    createEditOptionsKeyboard(),
                    databaseManager);
        }
        else if (step == 1) {
            switch (data) {
                case "edit_vegan" -> {
                    return askVeganQuestion(update);
                }
                case "edit_vegetarian" -> {
                    return askVegetarianQuestion(update);
                }
                case "edit_allergies" -> {
                    return askAllergiesQuestion(update);
                }
            }
        } else if (step == 2) {
            databaseManager.updateVegan(callbackQuery.getMessage().getChatId(), data.equals("edit_vegan_yes"));
            databaseManager.updateRegistrationStep(update.getCallbackQuery().getMessage().getChatId(), 5);
            return new EditMessageContainer(update,
                    "Your vegan status has been updated.",
                    createEmptyKeyboard(),
                    databaseManager);
        } else if (step == 4) {
            databaseManager.updateVegetarian(callbackQuery.getMessage().getChatId(), data.equals("edit_vegetarian_yes"));
            databaseManager.updateRegistrationStep(update.getCallbackQuery().getMessage().getChatId(), 5);
            return new EditMessageContainer(update,
                    "Your vegetarian status has been updated.",
                    createEmptyKeyboard(),
                    databaseManager);
        } else if (step == 6) {
            databaseManager.updateHasAllergies(callbackQuery.getMessage().getChatId(), data.equals("edit_allergies_yes"));
            if (databaseManager.hasAllergies(callbackQuery.getMessage().getChatId())) {
                return askAllergiesDetailsQuestion(update);
            } else {
                databaseManager.updateRegistrationStep(update.getCallbackQuery().getMessage().getChatId(), 5);
                return new EditMessageContainer(update,
                        "Your allergy status has been updated.",
                        createEmptyKeyboard(),
                        databaseManager);
            }
        } else if (step == 7) {
            databaseManager.updateAllergies(update.getMessage().getChatId(), translateService.translateToEnglish(update.getMessage().getText(), update.getMessage().getChatId()));
            databaseManager.updateRegistrationStep(update.getMessage().getChatId(), 5);
            return new EditMessageContainer(update,
                    "Your allergies have been updated.",
                    createEmptyKeyboard(),
                    databaseManager);
        }
        return null;
    }

    private InlineKeyboardButton createPut(String text, String data) {
        InlineKeyboardButton put = new InlineKeyboardButton();
        put.setText(text);
        put.setCallbackData(data);
        return put;
    }

    private InlineKeyboardMarkup createEmptyKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
}
