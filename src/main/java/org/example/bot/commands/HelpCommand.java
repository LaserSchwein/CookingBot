package org.example.bot.commands;

import org.example.bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.LinkedHashMap;
import java.util.Map;

public class HelpCommand implements Command {

    @Override
    public String getDescription() {
        return "Список команд";
    }

    @Override
    public String getContent() {
        StringBuilder helpMessage = new StringBuilder("Доступные команды:\n");
        for (Map.Entry<String, Command> entry : TelegramBot.getCommandMap().entrySet()) {
            helpMessage.append(entry.getKey()).append(" - ").append(entry.getValue().getDescription()).append("\n");
        }
        return helpMessage.toString();
    }

    @Override
    public String getCommand() {
        return "/help";
    }
}
