package org.example.service.impl;

import org.example.dao.RawDataDao;
import org.example.entity.RawData;
import org.example.service.MainService;
import org.example.service.ProducerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class MainServiceImpl implements MainService {

    private final RawDataDao rawDataDao;

    private final ProducerService producerService;

    public MainServiceImpl(RawDataDao rawDataDao, ProducerService producerService) {
        this.rawDataDao = rawDataDao;
        this.producerService =producerService;
    }


    @Override
    public void processedTextMessage(Update update) {

        saveRawData(update);

        SendMessage message = SendMessage
                .builder()
                .chatId(update.getMessage().getChatId())
                .text(update.getMessage().getText() + " - Echo from Node")
                .build();

        producerService.produceAnswer(message);

    }

    private void saveRawData(Update update) {

        RawData rawData = RawData.builder()
                          .event(update)
                          .build();

        rawDataDao.saveAndFlush(rawData);
    }
}
