package org.example.bot.commands;

import okhttp3.OkHttpClient;
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

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton addButton = new InlineKeyboardButton();
        addButton.setText("Add Product");
        addButton.setCallbackData("add_product");
        row1.add(addButton);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton deleteButton = new InlineKeyboardButton();
        deleteButton.setText("Delete Product");
        deleteButton.setCallbackData("delete_product");
        row2.add(deleteButton);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton viewButton = new InlineKeyboardButton();
        viewButton.setText("View List");
        viewButton.setCallbackData("view_list");
        row3.add(viewButton);

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        markup.setKeyboard(rows);
        sendMessage.setReplyMarkup(markup);
        sendMessage.setText("Choose what you want to do with your product list:");

        return sendMessage;
    }

    @Override
    public String getCommand() {
        return "/list";
    }

    @Override
    public InlineKeyboardMarkup createHelpBackButtonKeyboard() {
        return Command.super.createHelpBackButtonKeyboard();
    }

    public SendMessage handleCallback(Update update, String callbackData) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());

        switch (callbackData) {
            case "add_product":
                sendMessage.setText("Please enter the product you want to add:");
                currentAction = "add";
                break;
            case "delete_product":
                String products = databaseManager.getListOfProducts(update.getCallbackQuery().getMessage().getChatId());
                if (products == null || products.isEmpty()) {
                    sendMessage.setText("No products to delete.");
                } else {
                    sendMessage.setText(translateService.translateFromEnglish("Please enter the products you want to delete (comma-separated):", update.getCallbackQuery().getMessage().getChatId()) + "\n" + products);
                    currentAction = "delete";
                }
                break;
            case "view_list":
                String productList = databaseManager.getListOfProducts(update.getCallbackQuery().getMessage().getChatId());
                if (productList == null || productList.isEmpty()) {
                    sendMessage.setText("You don't have any products yet.");
                } else {
                    sendMessage.setText("Your products: " + productList);
                }
                break;
            default:
                sendMessage.setText("Invalid option.");
                break;
        }

        return sendMessage;
    }

    public SendMessage processProductInput(Update update, String input) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId().toString());

        logger.info("Processing product input: " + input + " for action: " + currentAction);

        if (currentAction != null) {
            if (currentAction.equals("add")) {
                databaseManager.addProduct(update.getMessage().getChatId(), input);
                sendMessage.setText("Product added successfully.");
            } else if (currentAction.equals("delete")) {
                boolean deleted = databaseManager.deleteProducts(update.getMessage().getChatId(), input);
                if (deleted) {
                    sendMessage.setText("Products deleted successfully.");
                } else {
                    sendMessage.setText("Some products were not found and could not be deleted.");
                }
            }
            currentAction = null;
        } else {
            sendMessage.setText("Invalid input.");
        }

        return sendMessage;
    }
}