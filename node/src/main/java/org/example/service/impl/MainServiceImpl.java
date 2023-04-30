package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDao;
import org.example.dao.RawDataDao;
import org.example.entity.AppDocument;
import org.example.entity.AppPhoto;
import org.example.entity.AppUser;
import org.example.entity.RawData;
import org.example.entity.enums.UserState;
import org.example.enums.LinkType;
import org.example.exception.UploadFileException;
import org.example.service.AppUserService;
import org.example.service.FileService;
import org.example.service.MainService;
import org.example.service.ProducerService;
import org.example.enums.ServiceCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

@Service
@Log4j
public class MainServiceImpl implements MainService {

    private final RawDataDao rawDataDao;

    private final AppUserDao appUserDao;

    private final FileService fileService;

    private final AppUserService appUserService;

    private final ProducerService producerService;

    public MainServiceImpl(RawDataDao rawDataDao,
                           AppUserDao appUserDao,
                           FileService fileService,
                           AppUserService appUserService,
                           ProducerService producerService) {
        this.rawDataDao = rawDataDao;
        this.appUserDao = appUserDao;
        this.fileService = fileService;
        this.appUserService = appUserService;
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

        else if (appUser.getState().equals(UserState.BASIC_STATE)) {

            outputText = processServiceCommand(appUser, textFromUpdate);
        }

        else if (appUser.getState().equals(UserState.WAIT_FOR_EMAIL_STATE)) {

           outputText = appUserService.setEmail(appUser, textFromUpdate);
        }

        sendAnswer(outputText, update);
    }

    @Override
    public void processedDocMessage(Update update) {

        saveRawData(update);

        AppUser appUser = findOrSaveAppUser(update);

        if (isNotAllowToSendContent(update)) {
            return;
        }

        try {
           AppDocument appDocument = fileService.processDoc(update.getMessage());

            String link = fileService.generatedLink(appDocument.getId(), LinkType.GET_DOC);

            String answer = "Document successfully downloading. \n" +
                    "link to download:\n" +
                    link;

            sendAnswer(answer, update);
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

            String link = fileService.generatedLink(appPhoto.getId(), LinkType.GET_PHOTO);

            String outputAnswer = "Photo successfully downloading.\n" +
                    "link for download:\n" +
                    link;

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

            return appUserService.registerUser(appUser);
        }

        if (ServiceCommand.HELP.equals(serviceCommand)) {

            return help();
        }

        if (ServiceCommand.START.equals(serviceCommand)) {

            return  "Welcome to HostingService bot application \n" +
                    "/help - to available command\n" +
                    "/registration - to register in app \n" +
                    "/cancel - to cancel current command";

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

        Optional<AppUser> optionalAppUser = appUserDao.findByTelegramUserId(telegramUser.getId());

        if(!optionalAppUser.isPresent()) {

            AppUser newUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .userName(telegramUser.getUserName())
                    .isActive(false)
                    .state(UserState.BASIC_STATE)
                    .build();
            return appUserDao.saveAndFlush(newUser);
        }

        return optionalAppUser.get();
    }

    private void saveRawData(Update update) {

        RawData rawData = RawData.builder()
                          .event(update)
                          .build();

        rawDataDao.saveAndFlush(rawData);
    }
}
