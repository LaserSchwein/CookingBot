package org.example.bot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class HelpCommand implements Command {
    @Override
    public SendMessage execute(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId().toString());
        sendMessage.setText("Я могу помочь вам с рецептами. Вот список доступных команд:\n" +
                "/start - Начать\n" +
                "/help - Помощь\n" +
                "/authors - Узнать авторов\n" +
                "/info - узнать информацию о боте" );

        return sendMessage;
    }
}
