package org.example.bot.commands;

import org.example.bot.DatabaseManager;
import org.example.bot.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

        if (update.hasMessage() && update.getMessage().hasText()) {
            userId = update.getMessage().getFrom().getId();
            userName = update.getMessage().getFrom().getUserName();

            if (step == 0) {
                user = new User(userId, userName);
                registerUser(user);
            } else if (step == 6){
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                message.setText("Вы уже зарегистрировались");
                return message;
            }

            CallbackQuery callbackQuery = new CallbackQuery();
            step = databaseManager.getRegistrationStep(update.getMessage().getChatId());
            return handleCallbackQuery(callbackQuery, update, step);
        } else if (update.hasCallbackQuery()) {
            step = databaseManager.getRegistrationStep(update.getCallbackQuery().getMessage().getChatId());

        } else {
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                message.setText("Ошибка регистрации: некорректный запрос.");
                return message;
        }

        System.out.println(step);
        return handleCallbackQuery(update.getCallbackQuery(), update, step);
    }

    private InlineKeyboardButton createPut(String language, String data) {
        InlineKeyboardButton put = new InlineKeyboardButton();
        put.setText(language);
        put.setCallbackData(data);
        return put;
    }

    private SendMessage askLanguageQuestion(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("На каком языке вы разговариваете?");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createPut("Русский", "language_ru"));
        row.add(createPut("Английский", "language_en"));
        row.add(createPut("Немецкий", "language_de"));
        row.add(createPut("Французский", "language_fr"));
        row.add(createPut("Испанский", "language_es"));
        rows.add(row);
        markup.setKeyboard(rows);

        message.setReplyMarkup(markup);

        // Сохраняем текущий шаг
        step = 1;
        databaseManager.updateRegistrationStep(user.getUserId(), step);

        return message;
    }

    private SendMessage askVeganQuestion(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
        message.setText("Вы веган?");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createPut("Да", "vegan_yes"));
        row.add(createPut("Нет", "vegan_no"));
        rows.add(row);
        markup.setKeyboard(rows);

        message.setReplyMarkup(markup);

        // Сохраняем текущий шаг
        step = 2;
        databaseManager.updateRegistrationStep(user.getUserId(), step);

        return message;
    }

    private SendMessage askVegetarianQuestion(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
        message.setText("Вы вегетарианец?");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createPut("Да", "vegetarian_yes"));
        row.add(createPut("Нет", "vegetarian_no"));
        rows.add(row);
        markup.setKeyboard(rows);

        message.setReplyMarkup(markup);

        // Сохраняем текущий шаг
        step = 3;
        databaseManager.updateRegistrationStep(user.getUserId(), step);

        return message;
    }

    private SendMessage askAllergiesQuestion(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
        message.setText("Есть ли у вас аллергии?");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createPut("Да", "allergies_yes"));
        row.add(createPut("Нет", "allergies_no"));
        rows.add(row);
        markup.setKeyboard(rows);

        message.setReplyMarkup(markup);

        // Сохраняем текущий шаг
        step = 4;
        databaseManager.updateRegistrationStep(user.getUserId(), step);

        return message;
    }

    private SendMessage askAllergiesDetailsQuestion(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
        message.setText("Какие у вас аллергии?");

        // Сохраняем текущий шаг
        step = 5;
        databaseManager.updateRegistrationStep(user.getUserId(), step);

        return message;
    }

    public SendMessage handleCallbackQuery(CallbackQuery callbackQuery, Update update, int step) {

        String data = new String();

        if (step != 0) {
            data = callbackQuery.getData();
        }
        if (step == 0) {
            return askLanguageQuestion(update);
        } else if (step == 1) {
            databaseManager.updateLanguage(user.getUserId(), data.replace("language_", ""));
            return askVeganQuestion(update);
        } else if (step == 2) {
            databaseManager.updateVegan(user.getUserId(), data.equals("vegan_yes"));
            return askVegetarianQuestion(update);
        } else if (step == 3) {
            databaseManager.updateVegetarian(user.getUserId(), data.equals("vegetarian_yes"));
            return askAllergiesQuestion(update);
        } else if (step == 4) {
            databaseManager.updateHasAllergies(user.getUserId(), data.equals("allergies_yes"));
            if (databaseManager.hasAllergies(user.getUserId())) {
                return askAllergiesDetailsQuestion(update);
            } else {
                SendMessage message = new SendMessage();
                message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
                message.setText("Вы зарегистрировались");
                return message;
            }
        } else if (step == 5) {
            databaseManager.updateAllergies(user.getUserId(), update.getMessage().getText());
            databaseManager.updateRegistrationStep(user.getUserId(), 6);
            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId().toString());
            message.setText("Вы зарегистрировались");
            return message;
        }
        return null;
    }

    public void registerUser(User user) {
        databaseManager.addUser(user);
    }
}
