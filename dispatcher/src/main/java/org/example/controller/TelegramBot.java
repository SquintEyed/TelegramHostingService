package org.example.controller;

import lombok.extern.log4j.Log4j;
import org.example.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;

@Component
@Log4j
public class TelegramBot extends TelegramWebhookBot {

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.uri}")
    private String botUri;

    @Value("${bot.token}")
    private String botToken;

    private final UpdateProcessor updateProcessor;

    private final MessageUtils messageUtils;

    public TelegramBot(UpdateProcessor updateProcessor, MessageUtils messageUtils) {
        this.updateProcessor = updateProcessor;
        this.messageUtils = messageUtils;
    }


    @PostConstruct
    public void init() {
        updateProcessor.registerBot(this);

        SetWebhook setWebhook = new SetWebhook(botUri);
        try {
            this.setWebhook(setWebhook);
        }
        catch (TelegramApiException ex) {
            log.error(ex);
        }

    }

    @Override
    public String getBotPath() {
        return "/update";
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
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
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
