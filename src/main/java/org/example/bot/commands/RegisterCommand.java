package org.example.bot.commands;

import org.example.bot.EditMessageContainer;
import org.example.bot.database.DatabaseManager;
import org.example.bot.database.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class RegisterCommand implements Command {
    private final DatabaseManager databaseManager;
    private User user;
    private int step = 0;

    public RegisterCommand(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public String getDescription() {
        return "Регистрация аккаунта в боте";
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
        long userId;
        String userName;

        if (update.hasMessage() && update.getMessage().hasText()){
            userId = update.getMessage().getFrom().getId();
            userName = update.getMessage().getFrom().getUserName();
        } else {
            userId = update.getCallbackQuery().getFrom().getId();
            userName = update.getCallbackQuery().getFrom().getUserName();
        }

        user = new User(userId, userName);
        registerUser(user);

        return askVeganQuestion(update);
    }

    private SendMessage askVeganQuestion(Update update) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createPut("Да", "vegan_yes"));
        row.add(createPut("Нет", "vegan_no"));
        rows.add(row);
        markup.setKeyboard(rows);

        SendMessage message = new SendMessage();

        if (update.hasMessage() && update.getMessage().hasText()) {
            message.setChatId(update.getMessage().getChatId());
        } else {
            message.setChatId(update.getCallbackQuery().getMessage().getChatId());
        }

        message.setText("Вы веган?");
        message.setReplyMarkup(markup);

        step = 1;
        databaseManager.updateRegistrationStep(user.getUserId(), step);

        return message;
    }

    public InlineKeyboardMarkup first_Keyboard(){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createPut("Да", "vegan_yes"));
        row.add(createPut("Нет", "vegan_no"));
        rows.add(row);

        markup.setKeyboard(rows);

        return markup;
    }

    public EditMessageContainer registration(Update update){
        if (user == null) {
            long userId;
            String userName;

            if (update.hasMessage() && update.getMessage().hasText()) {
                userId = update.getMessage().getFrom().getId();
                userName = update.getMessage().getFrom().getUserName();
            } else {
                userId = update.getCallbackQuery().getFrom().getId();
                userName = update.getCallbackQuery().getFrom().getUserName();
            }

            user = new User(userId, userName);
        }


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
        firstRow.add(createPut("Да", "vegetarian_yes"));
        firstRow.add(createPut("Нет", "vegetarian_no"));
        rows.add(firstRow);

        List<InlineKeyboardButton> secondRow = new ArrayList<>();
        secondRow.add(createPut("⬅️ Назад", "back"));
        rows.add(secondRow);

        markup.setKeyboard(rows);

        step = 2;
        databaseManager.updateRegistrationStep(user.getUserId(), step);

        return new EditMessageContainer(update,
                "Вы вегетарианец?",
                markup);
    }

    private EditMessageContainer askAllergiesQuestion(Update update) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        firstRow.add(createPut("Да", "allergies_yes"));
        firstRow.add(createPut("Нет", "allergies_no"));
        rows.add(firstRow);

        List<InlineKeyboardButton> secondRow = new ArrayList<>();
        secondRow.add(createPut("⬅️ Назад", "back"));
        rows.add(secondRow);

        markup.setKeyboard(rows);

        step = 3;
        databaseManager.updateRegistrationStep(user.getUserId(), step);

        return new EditMessageContainer(update,
                "Есть ли у вас аллергии?",
                markup);
    }

    private EditMessageContainer askAllergiesDetailsQuestion(Update update) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createPut("⬅️ Назад", "back"));
        rows.add(row);

        markup.setKeyboard(rows);

        step = 4;
        databaseManager.updateRegistrationStep(user.getUserId(), step);

        return new EditMessageContainer(update,
                "Какие у вас аллергии?",
                markup);
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
                    "Вы веган?",
                    first_Keyboard());
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
                        "Вы зарегистрировались",
                        createEmptyKeyboard());
            }
        } else if (step == 4) {
            databaseManager.updateAllergies(user.getUserId(), update.getMessage().getText());
            databaseManager.updateRegistrationStep(user.getUserId(), 5);
            return new EditMessageContainer(update,
                    "Вы зарегистрировались",
                    createEmptyKeyboard());
        }
        return null;
    }

    public void registerUser(User user) {
        databaseManager.addUser(user);
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
