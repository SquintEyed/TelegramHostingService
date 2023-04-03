package org.example.controller;

import lombok.extern.log4j.Log4j;
import org.example.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;

@Component
@Log4j
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    private final UpdateController updateController;
    private final MessageUtils messageUtils;

    public TelegramBot(UpdateController updateController, MessageUtils messageUtils) {
        this.updateController = updateController;
        this.messageUtils = messageUtils;
    }


    @PostConstruct
    public void init() {
        updateController.registerBot(this);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {

        updateController.processUpdate(update);

    }

    public void sendAnswerMessage(SendMessage sendMessage) {

        try {
            this.execute(sendMessage);
        }

        catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}
