package org.example.controller;

import lombok.extern.log4j.Log4j;
import org.example.service.UpdateProducer;
import org.example.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import static org.example.model.RabbitQueue.*;

import java.util.Objects;

@Component
@Log4j
public class UpdateProcessor {

    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;

    public UpdateProcessor(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }


    public void registerBot(TelegramBot telegramBot){
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update) {

        if (update == null) {
            log.error("Received update is null");
            return;
        }

        if (update.hasMessage()) {
            distributeMessagesByType(update);
            return;
        }

        log.error("unsupported type message" + update);
    }

    private void distributeMessagesByType(Update update) {

        Message message = update.getMessage();

        if (message.hasText()) {
            processTextMessage(update);
        }

        else if (message.hasDocument()) {
            processDocumentMessage(update);
        }

        else if (message.hasPhoto()) {
            processPhotoMessage(update);
        }

        else {
            setUnsupportedMessageType(update);
        }
    }

    private void processPhotoMessage(Update update) {

        updateProducer.produce(PHOTO_MESSAGE_UPDATE, update);

        setFileIsReceivedView(update);
    }

    private void processDocumentMessage(Update update) {

        updateProducer.produce(DOC_MESSAGE_UPDATE, update);

        setFileIsReceivedView(update);
    }

    private void processTextMessage(Update update) {

        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
    }

    private void setUnsupportedMessageType(Update update) {

        SendMessage sendMessage = messageUtils
                .generatedSendMessageWithText(update, "Unsupported message type");

        setView(sendMessage);
    }

    public void setView(SendMessage sendMessage) {

        telegramBot.sendAnswerMessage(sendMessage);
    }

    private void setFileIsReceivedView(Update update) {

        setView(messageUtils.generatedSendMessageWithText(update, "File received and processed ..."));
    }
}
