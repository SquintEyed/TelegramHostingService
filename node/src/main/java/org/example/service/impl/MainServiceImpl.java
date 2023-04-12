package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDao;
import org.example.dao.RawDataDao;
import org.example.entity.AppDocument;
import org.example.entity.AppPhoto;
import org.example.entity.AppUser;
import org.example.entity.RawData;
import org.example.entity.enums.UserState;
import org.example.exception.UploadFileException;
import org.example.service.FileService;
import org.example.service.MainService;
import org.example.service.ProducerService;
import org.example.service.enums.ServiceCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Objects;

@Service
@Log4j
public class MainServiceImpl implements MainService {

    private final RawDataDao rawDataDao;

    private final AppUserDao appUserDao;

    private final FileService fileService;

    private final ProducerService producerService;

    public MainServiceImpl(RawDataDao rawDataDao,
                           AppUserDao appUserDao,
                           FileService fileService,
                           ProducerService producerService) {
        this.rawDataDao = rawDataDao;
        this.appUserDao = appUserDao;
        this.fileService = fileService;
        this.producerService = producerService;
    }


    @Override
    public void processedTextMessage(Update update) {

        saveRawData(update);

        AppUser appUser = findOrSaveAppUser(update);

        String textFromUpdate = update.getMessage().getText();
        String outputText = "";

        if (ServiceCommand.CANCEL.equals(ServiceCommand.fromValue(textFromUpdate))) {

            outputText = cancelProcess(appUser);
        }

        if (appUser.getState().equals(UserState.BASIC_STATE)) {

            outputText = processServiceCommand(appUser, textFromUpdate);
        }

        if (appUser.getState().equals(UserState.WAIT_FOR_EMAIL_STATE)) {

            //TODO make later
        }

        sendAnswer(outputText, update);
    }

    @Override
    public void processedDocMessage(Update update) {

        saveRawData(update);

        //TODO make method to save document later...

        AppUser appUser = findOrSaveAppUser(update);

        if (isNotAllowToSendContent(update)) {
            return;
        }

        try {
           AppDocument appDocument = fileService.processDoc(update.getMessage());

            String outputAnswer = "Document successfully downloading, url=yandex-doc.com";

            sendAnswer(outputAnswer,update);
        }

        catch (UploadFileException ex) {
            log.error(ex);
            String error = "Error!!";
            sendAnswer(error, update);
        }

    }

    @Override
    public void processedPhotoMessage(Update update) {

        saveRawData(update);

        if (isNotAllowToSendContent(update)) {
            return;
        }

        try {
            AppPhoto appPhoto = fileService.processPhoto(update.getMessage());

            String outputAnswer = "Photo successfully downloading, url=yandex-photo.com";

            sendAnswer(outputAnswer, update);
        }

        catch (UploadFileException ex) {
            log.error(ex);
            String error = "Error!!";
            sendAnswer(error, update);
        }
    }

    private boolean isNotAllowToSendContent(Update update) {

        AppUser appUser = findOrSaveAppUser(update);

        String errorAnswer;

        if (!appUser.isActive()) {

            errorAnswer = "Register or confirm registration-mail";

            sendAnswer(errorAnswer, update);

            return true;
        }

        if (appUser.getState().equals(UserState.WAIT_FOR_EMAIL_STATE)) {

            errorAnswer = "you must be registered to use this feature";

            sendAnswer(errorAnswer, update);

            return true;
        }

        return false;
    }

    private void sendAnswer(String outputText, Update update) {

        SendMessage message = SendMessage
                .builder()
                .chatId(update.getMessage().getChatId())
                .text(outputText)
                .build();

        producerService.produceAnswer(message);

    }

    private String processServiceCommand(AppUser appUser, String cmd) {

        ServiceCommand serviceCommand = ServiceCommand.fromValue(cmd);

        if (ServiceCommand.REGISTRATION.equals(serviceCommand)) {

            return "Error, command unavailable";
        }

        if (ServiceCommand.HELP.equals(serviceCommand)) {

            return help();
        }

        if (ServiceCommand.START.equals(serviceCommand)) {

            return  "Welcome to HostingService bot application";
        }

        return "Error, unknown command, enter /help to continue";
    }

    private String help() {

        return "Available commands from bot \n" +
                "/cancel - to cancel current command \n" +
                "/start - to start app work \n" +
                "/registration - to register in app";
    }

    private String cancelProcess(AppUser appUser) {

        appUser.setState(UserState.BASIC_STATE);

        appUserDao.saveAndFlush(appUser);

        return "command cancelled";
    }

    private AppUser findOrSaveAppUser(Update update) {

        User telegramUser = update.getMessage().getFrom();

        AppUser persistentAppUser = appUserDao.findAppUserByTelegramUserId(telegramUser.getId());

        if(Objects.isNull(persistentAppUser)) {

            AppUser newUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .userName(telegramUser.getUserName())
                    //TODO изменить после доработки кода
                    .isActive(true)
                    .state(UserState.BASIC_STATE)
                    .build();
            return appUserDao.saveAndFlush(newUser);

        }

        return persistentAppUser;
    }

    private void saveRawData(Update update) {

        RawData rawData = RawData.builder()
                          .event(update)
                          .build();

        rawDataDao.saveAndFlush(rawData);
    }
}
