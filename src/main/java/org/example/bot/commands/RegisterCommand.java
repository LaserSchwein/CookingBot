package org.example.bot.commands;

import okhttp3.OkHttpClient;
import org.example.bot.EditMessageContainer;
import org.example.bot.api.TranslateService;
import org.example.bot.database.DatabaseManager;
import org.example.bot.database.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class RegisterCommand implements Command {
    private final DatabaseManager databaseManager;
    private final TranslateService translateService;
    private User user;
    private int step = 0;

    public RegisterCommand(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        OkHttpClient okHttpClient = new OkHttpClient();
        this.translateService = new TranslateService(okHttpClient, databaseManager);
    }

    @Override
    public String getDescription() {
        return "Registering an account in the bot";
    }

    @Override
    public String getCommand() {
        return "/register";
    }

    @Override
    public InlineKeyboardMarkup createHelpBackButtonKeyboard() {
        return Command.super.createHelpBackButtonKeyboard();
    }

    @Override
    public SendMessage getContent(Update update) {


        return askVeganQuestion(update);
    }

    private SendMessage askVeganQuestion(Update update) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createPut("✅", "vegan_yes"));
        row.add(createPut("❎", "vegan_no"));
        rows.add(row);
        markup.setKeyboard(rows);

        SendMessage message = new SendMessage();

        long chatId;
        if (update.hasMessage() && update.getMessage().hasText()) {
            chatId = update.getMessage().getChatId();
        } else {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }

        message.setChatId(chatId);
        message.setText("Are you vegan?");
        message.setReplyMarkup(markup);

        step = 1;
        this.user = new User(chatId, "");
        databaseManager.updateRegistrationStep(user.getUserId(), step);

        return message;
    }

    public InlineKeyboardMarkup first_Keyboard(){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createPut("✅", "vegan_yes"));
        row.add(createPut("❎", "vegan_no"));
        rows.add(row);

        markup.setKeyboard(rows);

        return markup;
    }

    public EditMessageContainer registration(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            step = databaseManager.getRegistrationStep(update.getMessage().getChatId());
            System.out.println("Step from message: " + step);
        } else {
            step = databaseManager.getRegistrationStep(update.getCallbackQuery().getMessage().getChatId());
            System.out.println("Step from callback: " + step);
        }

        System.out.println("Final step: " + step);
        return handleCallbackQuery(update.getCallbackQuery(), update, step);
    }

    private EditMessageContainer askVegetarianQuestion(Update update) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        firstRow.add(createPut("✅", "vegetarian_yes"));
        firstRow.add(createPut("❎", "vegetarian_no"));
        rows.add(firstRow);

        List<InlineKeyboardButton> secondRow = new ArrayList<>();
        secondRow.add(createPut("⬅️", "back"));
        rows.add(secondRow);

        markup.setKeyboard(rows);

        step = 2;
        databaseManager.updateRegistrationStep(user.getUserId(), step);

        return new EditMessageContainer(update,
                "Are you a vegetarian?",
                markup,
                databaseManager);
    }

    private EditMessageContainer askAllergiesQuestion(Update update) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        firstRow.add(createPut("✅", "allergies_yes"));
        firstRow.add(createPut("❎", "allergies_no"));
        rows.add(firstRow);

        List<InlineKeyboardButton> secondRow = new ArrayList<>();
        secondRow.add(createPut("⬅️", "back"));
        rows.add(secondRow);

        markup.setKeyboard(rows);

        step = 3;
        databaseManager.updateRegistrationStep(user.getUserId(), step);

        return new EditMessageContainer(update,
                "Do you have any allergies?",
                markup,
                databaseManager);
    }

    private EditMessageContainer askAllergiesDetailsQuestion(Update update) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createPut("⬅️", "back"));
        rows.add(row);

        markup.setKeyboard(rows);

        step = 4;
        databaseManager.updateRegistrationStep(user.getUserId(), step);

        return new EditMessageContainer(update,
                "What allergies do you have?",
                markup,
                databaseManager);
    }

    public EditMessageContainer handleCallbackQuery(CallbackQuery callbackQuery, Update update, int step) {
        String data = "";

        if (update.hasCallbackQuery()) {
            data = callbackQuery.getData();
        }

        if (data.equals("back")){
            step -= 2;
        }

        if (step == 0) {
            databaseManager.updateRegistrationStep(user.getUserId(), 1);

            return new EditMessageContainer(update,
                    "Are you vegan?",
                    first_Keyboard(),
                    databaseManager);
        } else if (step == 1) {
            databaseManager.updateVegan(user.getUserId(), data.equals("vegan_yes"));
            return askVegetarianQuestion(update);
        } else if (step == 2) {
            databaseManager.updateVegetarian(user.getUserId(), data.equals("vegetarian_yes"));
            return askAllergiesQuestion(update);
        } else if (step == 3) {
            databaseManager.updateHasAllergies(user.getUserId(), data.equals("allergies_yes"));
            if (databaseManager.hasAllergies(user.getUserId())) {
                return askAllergiesDetailsQuestion(update);
            } else {
                return new EditMessageContainer(update,
                        "You have registered",
                        createEmptyKeyboard(),
                        databaseManager);
            }
        } else if (step == 4) {
            databaseManager.updateAllergies(user.getUserId(), translateService.translateToEnglish(update.getMessage().getText(), update.getMessage().getChatId()));
            databaseManager.updateRegistrationStep(user.getUserId(), 5);
            return new EditMessageContainer(update,
                    "You have registered",
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
