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
    private final DatabaseManager databaseManager = new DatabaseManager();
    private User user;
    private int step = 0;

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

        EditMessageContainer editMessageContainer = new EditMessageContainer(
                crateEditMessageText(update, "Вы вегетарианец?"),
                crateEditMessageReplyMarkup(update, markup));

        step = 2;
        databaseManager.updateRegistrationStep(user.getUserId(), step);

        return editMessageContainer;
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

        EditMessageContainer editMessageContainer = new EditMessageContainer(
                crateEditMessageText(update, "Есть ли у вас аллергии?"),
                crateEditMessageReplyMarkup(update, markup));

        step = 3;
        databaseManager.updateRegistrationStep(user.getUserId(), step);

        return editMessageContainer;
    }

    private EditMessageContainer askAllergiesDetailsQuestion(Update update) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createPut("⬅️ Назад", "back"));
        rows.add(row);

        markup.setKeyboard(rows);

        EditMessageContainer editMessageContainer = new EditMessageContainer(
                crateEditMessageText(update, "Какие у вас аллергии?"),
                crateEditMessageReplyMarkup(update, markup));

        step = 4;
        databaseManager.updateRegistrationStep(user.getUserId(), step);

        return editMessageContainer;
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


            return new EditMessageContainer(
                    crateEditMessageText(update, "Вы веган?"),
                    crateEditMessageReplyMarkup(update, first_Keyboard()));
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
                return new EditMessageContainer(
                        crateEditMessageText(update, "Вы зарегистрировались"),
                        crateEditMessageReplyMarkup(update, new InlineKeyboardMarkup()));
            }
        } else if (step == 4) {
            databaseManager.updateAllergies(user.getUserId(), update.getMessage().getText());
            databaseManager.updateRegistrationStep(user.getUserId(), 5);
            return new EditMessageContainer(
                    crateEditMessageText(update, "Вы зарегистрировались"),
                    crateEditMessageReplyMarkup(update, new InlineKeyboardMarkup()));
        }
        return null;
    }

    public void registerUser(User user) {
        databaseManager.addUser(user);
    }

    private InlineKeyboardButton createPut(String language, String data) {
        InlineKeyboardButton put = new InlineKeyboardButton();
        put.setText(language);
        put.setCallbackData(data);
        return put;
    }

    private EditMessageText crateEditMessageText(Update update, String question) {
        EditMessageText editMessageText = new EditMessageText();

        if (update.hasMessage() && update.getMessage().hasText()){
            editMessageText.setChatId(update.getMessage().getChatId().toString());
            editMessageText.setMessageId(update.getMessage().getMessageId());
        } else {
            editMessageText.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
            editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        }

        editMessageText.setText(question);

        return editMessageText;
    }

    private EditMessageReplyMarkup crateEditMessageReplyMarkup(Update update, InlineKeyboardMarkup markup) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();

        if (update.hasMessage() && update.getMessage().hasText()){
            editMessageReplyMarkup.setChatId(update.getMessage().getChatId().toString());
            editMessageReplyMarkup.setMessageId(update.getMessage().getMessageId());
        } else {
            editMessageReplyMarkup.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
            editMessageReplyMarkup.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        }

        editMessageReplyMarkup.setReplyMarkup(markup);

        return editMessageReplyMarkup;
    }
}
