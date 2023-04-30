package org.example.controller;

import org.example.dto.MailParams;
import org.example.service.MailSenderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MailServiceController {

    private final MailSenderService mailSenderService;

    public MailServiceController(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }


    @PostMapping("/mail/send")
    public ResponseEntity<?> sendActivationMail(@RequestBody MailParams mailParams) {
        mailSenderService.send(mailParams);
        return ResponseEntity.ok().build();
    }
}
