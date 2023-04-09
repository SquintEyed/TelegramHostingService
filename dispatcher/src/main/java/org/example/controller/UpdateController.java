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
public class UpdateController {

    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;

    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }


    public void registerBot(TelegramBot telegramBot){
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update) {

        if (Objects.isNull(update)) {
            log.error("Received update is null");
            return;
        }

        distributeMessagesByType(update);
    }

    private void distributeMessagesByType(Update update) {

        Message message = update.getMessage();

        if (message.getText() != null) {
            processTextMessage(update);
            return;
        }

        if (message.getDocument() != null) {
            processDocumentMessage(update);
            return;
        }

        if (message.getPhoto() !=  null) {
            processPhotoMessage(update);
            return;
        }

        setUnsupportedMessageType(update);
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

       // setFileIsReceivedView(update);
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
