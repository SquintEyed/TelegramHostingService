package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDao;
import org.example.dto.MailParams;
import org.example.entity.AppUser;
import org.example.entity.enums.UserState;
import org.example.service.AppUserService;
import org.example.utils.CryptoTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Optional;

@Service
@Log4j
public class AppUserServiceImpl implements AppUserService {

    @Value("${service.mail.uri}")
    private String mailServiceUri;

    private final AppUserDao appUserDao;
    private final CryptoTool cryptoTool;

    public AppUserServiceImpl(AppUserDao appUserDao, CryptoTool cryptoTool) {
        this.appUserDao = appUserDao;
        this.cryptoTool = cryptoTool;
    }


    @Override
    public String registerUser(AppUser appUser) {

        if (appUser.isActive()) {
            return "Your account already activated";
        }

        if (appUser.getEmail() != null) {
            return "Check your email and follow the link to complete registration";
        }

        appUser.setState(UserState.WAIT_FOR_EMAIL_STATE);

        appUserDao.save(appUser);

        return "Enter your email";
    }

    @Override
    public String setEmail(AppUser appUser, String email) {

        try {
            InternetAddress emailAddress = new InternetAddress(email);
            emailAddress.validate();
        }

        catch (AddressException ex) {
            return "Email is not correct, enter /cancel to cancel command";
        }

        Optional<AppUser> optionalAppUser = appUserDao.findByEmail(email);

        if (!optionalAppUser.isPresent()) {

            appUser.setEmail(email);
            appUser.setState(UserState.BASIC_STATE);
            appUser = appUserDao.save(appUser);

            String cryptoUserId = cryptoTool.hashOf(appUser.getId());

            ResponseEntity<String> response = sendRequestToMailService(cryptoUserId, email);

            if (response.getStatusCode() != HttpStatus.OK) {

                String msg = String.format("Sending message on email %s is failed",email);

                log.error(msg);

                appUser.setEmail(null);

                appUserDao.save(appUser);

                return msg;
            }

            return "Check your email for complete registration";
        }

        return String.format("Email %s already exist",email);
    }

    private ResponseEntity<String> sendRequestToMailService(String cryptoUserId, String email) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders header = new HttpHeaders();

        header.setContentType(MediaType.APPLICATION_JSON);

        MailParams mailParams = MailParams.builder()
                .id(cryptoUserId)
                .emailTo(email)
                .build();

        HttpEntity<MailParams> request = new HttpEntity<>(mailParams, header);

        return  restTemplate.exchange(
                mailServiceUri,
                HttpMethod.POST,
                request,
                String.class);
    }
}
