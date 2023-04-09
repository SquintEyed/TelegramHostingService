package org.example.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface MainService {

    void processedTextMessage(Update update);

    void processedDocMessage(Update update);

    void processedPhotoMessage(Update update);

}
