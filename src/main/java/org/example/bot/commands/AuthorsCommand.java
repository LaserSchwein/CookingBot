package org.example.bot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
public class AuthorsCommand implements Command {
    public String getDescription (){
        return "Авторы";
    }

    @Override
    public String getContent() {
        return "@ZAntoshkAZ @polska_stronker @sobol_eg";
    }

    @Override
    public String getCommand() {
        return "/authors";
    }

}
