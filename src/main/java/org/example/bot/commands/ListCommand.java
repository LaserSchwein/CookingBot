package org.example.bot.commands;

import okhttp3.OkHttpClient;
import org.example.bot.EditMessageContainer;
import org.example.bot.database.DatabaseManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.example.bot.api.TranslateService;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ListCommand implements Command {
    private final DatabaseManager databaseManager;
    String currentAction;
    private static final Logger logger = Logger.getLogger(ListCommand.class.getName());
    private final TranslateService translateService;


    public ListCommand(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.currentAction = null;
        OkHttpClient okHttpClient = new OkHttpClient();
        this.translateService = new TranslateService(okHttpClient, databaseManager);
    }

    @Override
    public String getDescription() {
        return "Manage your list of products.";
    }

    @Override
    public SendMessage getContent(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId().toString());
        sendMessage.setReplyMarkup(createKeyboardMarkup(update));
        sendMessage.setText(translateService.translateFromEnglish("Choose what you want to do with your product list:", update.getMessage().getChatId()));
        return sendMessage;
    }

    private InlineKeyboardMarkup createKeyboardMarkup(Update update) {
        Long chatId;

        if (update.getMessage() == null) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton addButton = new InlineKeyboardButton();
        addButton.setText(translateService.translateFromEnglish("Add Product", chatId));
        addButton.setCallbackData("add_product");
        row1.add(addButton);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton deleteButton = new InlineKeyboardButton();
        deleteButton.setText(translateService.translateFromEnglish("Delete Product", chatId));
        deleteButton.setCallbackData("delete_product");
        row2.add(deleteButton);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton viewButton = new InlineKeyboardButton();
        viewButton.setText(translateService.translateFromEnglish("View List", chatId));
        viewButton.setCallbackData("view_list");
        row3.add(viewButton);

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        markup.setKeyboard(rows);
        return markup;
    }


    @Override
    public String getCommand() {
        return "/list";
    }

    @Override
    public InlineKeyboardMarkup createHelpBackButtonKeyboard() {
        return Command.super.createHelpBackButtonKeyboard();
    }

    public EditMessageContainer handleCallback(Update update, String callbackData) {
        String text;

        switch (callbackData) {
            case "add_product":
                text = "Please enter the product you want to add:";
                currentAction = "add";
                break;
            case "delete_product":
                String products = databaseManager.getListOfProducts(update.getCallbackQuery().getMessage().getChatId());
                if (products == null || products.isEmpty()) {
                    text = "No products to delete.";
                } else {
                    text = "Please enter the products you want to delete (comma-separated):";
                    currentAction = "delete";
                }
                break;
            case "view_list":
                String productList = databaseManager.getListOfProducts(update.getCallbackQuery().getMessage().getChatId());
                if (productList == null || productList.isEmpty()) {
                    text = "You don't have any products yet.";
                } else {
                    text = "Your products: " + productList;
                }
                break;
            case "/list":
                text = "Choose what you want to do with your product list:";
                break;
            default:
                text = "Invalid option.";
                break;
        }

        return new EditMessageContainer(update,
                translateService.translateFromEnglish(text, update.getCallbackQuery().getMessage().getChatId()),
                createKeyboardMarkup(update),
                databaseManager);
    }

    public SendMessage processProductInput(Update update, String input) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId().toString());
        String text = "";
        logger.info("Processing product input: " + input + " for action: " + currentAction);

        if (currentAction != null) {
            if (currentAction.equals("add")) {
                databaseManager.addProduct(update.getMessage().getChatId(), input);
                text = "Product added successfully.";
            } else if (currentAction.equals("delete")) {
                boolean deleted = databaseManager.deleteProducts(update.getMessage().getChatId(), input);
                if (deleted) {
                    text = "Products deleted successfully.";
                } else {
                    text = "Some products were not found and could not be deleted.";
                }
            }
            currentAction = null; // Reset the action after processing
        } else {
            text = "Invalid input.";
        }

        sendMessage.setText(translateService.translateFromEnglish(text, update.getMessage().getChatId()));
        return sendMessage;
    }
}
