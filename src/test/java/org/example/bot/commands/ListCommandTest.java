package org.example.bot.commands;

import org.example.bot.EditMessageContainer;
import org.example.bot.database.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListCommandTest {

    private DatabaseManager databaseManagerMock;
    private ListCommand listCommand;
    private Update updateMock;
    private Message messageMock;
    private CallbackQuery callbackQueryMock;

    @BeforeEach
    void setUp() {
        databaseManagerMock = Mockito.mock(DatabaseManager.class);
        listCommand = new ListCommand(databaseManagerMock);
        updateMock = Mockito.mock(Update.class);
        messageMock = Mockito.mock(Message.class);
        callbackQueryMock = Mockito.mock(CallbackQuery.class);
    }

    @Test
    void testGetDescription() {
        String description = listCommand.getDescription();
        assertEquals("Manage your list of products.", description);
    }

    @Test
    void testGetContent() {
        when(updateMock.getMessage()).thenReturn(messageMock);
        when(messageMock.getChatId()).thenReturn(12345L);
        long chatId = 12345L;
        when(databaseManagerMock.getLanguage(chatId)).thenReturn("en");

        SendMessage sendMessage = listCommand.getContent(updateMock);
        assertNotNull(sendMessage);
        assertEquals("Choose what you want to do with your product list:", sendMessage.getText());

        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) sendMessage.getReplyMarkup();
        assertNotNull(markup);

        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        assertEquals(3, keyboard.size());

        assertEquals("Add Product", keyboard.get(0).get(0).getText());
        assertEquals("add_product", keyboard.get(0).get(0).getCallbackData());

        assertEquals("Delete Product", keyboard.get(1).get(0).getText());
        assertEquals("delete_product", keyboard.get(1).get(0).getCallbackData());

        assertEquals("View List", keyboard.get(2).get(0).getText());
        assertEquals("view_list", keyboard.get(2).get(0).getCallbackData());
    }

    @Test
    void testGetCommand() {
        String command = listCommand.getCommand();
        assertEquals("/list", command);
    }

    @Test
    void testHandleCallbackAddProduct() {
        when(updateMock.getCallbackQuery()).thenReturn(callbackQueryMock);
        when(callbackQueryMock.getMessage()).thenReturn(messageMock);
        when(messageMock.getChatId()).thenReturn(12345L);
        long chatId = 12345L;
        when(databaseManagerMock.getLanguage(chatId)).thenReturn("en");

        EditMessageContainer container = listCommand.handleCallback(updateMock, "add_product");
        assertNotNull(container);
        assertEquals("Please enter the product you want to add:", container.getEditMessageText());
    }

    @Test
    void testHandleCallbackDeleteProduct() {
        long chatId = 12345L;
        when(databaseManagerMock.getLanguage(chatId)).thenReturn("en");
        when(updateMock.getCallbackQuery()).thenReturn(callbackQueryMock);
        when(callbackQueryMock.getMessage()).thenReturn(messageMock);
        when(messageMock.getChatId()).thenReturn(12345L);
        when(databaseManagerMock.getListOfProducts(12345L)).thenReturn("milk, eggs, bread");

        EditMessageContainer container = listCommand.handleCallback(updateMock, "delete_product");
        assertNotNull(container);
        assertEquals("Please enter the products you want to delete (comma-separated):", container.getEditMessageText());
    }

    @Test
    void testHandleCallbackDeleteProductNoProducts() {
        when(updateMock.getCallbackQuery()).thenReturn(callbackQueryMock);
        when(callbackQueryMock.getMessage()).thenReturn(messageMock);
        when(messageMock.getChatId()).thenReturn(12345L);
        when(databaseManagerMock.getListOfProducts(12345L)).thenReturn(null);
        long chatId = 12345L;
        when(databaseManagerMock.getLanguage(chatId)).thenReturn("en");

        EditMessageContainer container = listCommand.handleCallback(updateMock, "delete_product");
        assertNotNull(container);
        assertEquals("No products to delete.", container.getEditMessageText());
    }

    @Test
    void testHandleCallbackViewList() {
        when(updateMock.getCallbackQuery()).thenReturn(callbackQueryMock);
        when(callbackQueryMock.getMessage()).thenReturn(messageMock);
        when(messageMock.getChatId()).thenReturn(12345L);
        when(databaseManagerMock.getListOfProducts(12345L)).thenReturn("milk, eggs, bread");
        long chatId = 12345L;
        when(databaseManagerMock.getLanguage(chatId)).thenReturn("en");

        EditMessageContainer container = listCommand.handleCallback(updateMock, "view_list");
        assertNotNull(container);
        assertEquals("Your products: milk, eggs, bread", container.getEditMessageText());
    }

    @Test
    void testHandleCallbackViewListNoProducts() {
        when(updateMock.getCallbackQuery()).thenReturn(callbackQueryMock);
        when(callbackQueryMock.getMessage()).thenReturn(messageMock);
        when(messageMock.getChatId()).thenReturn(12345L);
        when(databaseManagerMock.getListOfProducts(12345L)).thenReturn(null);
        long chatId = 12345L;
        when(databaseManagerMock.getLanguage(chatId)).thenReturn("en");

        EditMessageContainer container = listCommand.handleCallback(updateMock, "view_list");
        assertNotNull(container);
        assertEquals("You don't have any products yet.", container.getEditMessageText());
    }

    @Test
    void testHandleCallbackInvalidOption() {
        when(updateMock.getCallbackQuery()).thenReturn(callbackQueryMock);
        when(callbackQueryMock.getMessage()).thenReturn(messageMock);
        when(messageMock.getChatId()).thenReturn(12345L);
        long chatId = 12345L;
        when(databaseManagerMock.getLanguage(chatId)).thenReturn("en");

        EditMessageContainer container = listCommand.handleCallback(updateMock, "invalid_option");
        assertNotNull(container);
        assertEquals("Invalid option.", container.getEditMessageText());
    }

    @Test
    void testProcessProductInputAddProduct() {
        when(updateMock.getMessage()).thenReturn(messageMock);
        when(messageMock.getChatId()).thenReturn(12345L);
        listCommand.currentAction = "add";
        long chatId = 12345L;
        when(databaseManagerMock.getLanguage(chatId)).thenReturn("en");

        SendMessage sendMessage = listCommand.processProductInput(updateMock, "milk");
        assertNotNull(sendMessage);
        assertEquals("Product added successfully.", sendMessage.getText());

        verify(databaseManagerMock, times(1)).addProduct(12345L, "milk");
    }

    @Test
    void testProcessProductInputDeleteProduct() {
        when(updateMock.getMessage()).thenReturn(messageMock);
        when(messageMock.getChatId()).thenReturn(12345L);
        listCommand.currentAction = "delete";
        when(databaseManagerMock.deleteProducts(12345L, "milk")).thenReturn(true);
        long chatId = 12345L;
        when(databaseManagerMock.getLanguage(chatId)).thenReturn("en");

        SendMessage sendMessage = listCommand.processProductInput(updateMock, "milk");
        assertNotNull(sendMessage);
        assertEquals("Products deleted successfully.", sendMessage.getText());

        verify(databaseManagerMock, times(1)).deleteProducts(12345L, "milk");
    }

    @Test
    void testProcessProductInputDeleteProductNotFound() {
        when(updateMock.getMessage()).thenReturn(messageMock);
        when(messageMock.getChatId()).thenReturn(12345L);
        listCommand.currentAction = "delete";
        when(databaseManagerMock.deleteProducts(12345L, "milk")).thenReturn(false);
        long chatId = 12345L;
        when(databaseManagerMock.getLanguage(chatId)).thenReturn("en");

        SendMessage sendMessage = listCommand.processProductInput(updateMock, "milk");
        assertNotNull(sendMessage);
        assertEquals("Some products were not found and could not be deleted.", sendMessage.getText());

        verify(databaseManagerMock, times(1)).deleteProducts(12345L, "milk");
    }

    @Test
    void testProcessProductInputInvalidAction() {
        when(updateMock.getMessage()).thenReturn(messageMock);
        when(messageMock.getChatId()).thenReturn(12345L);
        listCommand.currentAction = null;
        long chatId = 12345L;
        when(databaseManagerMock.getLanguage(chatId)).thenReturn("en");

        SendMessage sendMessage = listCommand.processProductInput(updateMock, "milk");
        assertNotNull(sendMessage);
        assertEquals("Invalid input.", sendMessage.getText());
    }
}
