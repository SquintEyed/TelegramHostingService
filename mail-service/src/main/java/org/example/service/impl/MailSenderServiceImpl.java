package org.example.service.impl;

import org.example.dto.MailParams;
import org.example.service.MailSenderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailSenderServiceImpl implements MailSenderService {

    @Value("${spring.mail.username}")
    private String emailFrom;
    @Value("${service.activation.uri}")
    private String activationServiceUri;

    private final JavaMailSender javaMailSender;

    public MailSenderServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }


    @Override
    public void send(MailParams mailParams) {

        String subject = "Activation your account";
        String messageBody = getActivationMailBody(mailParams.getId());
        String emailTo = mailParams.getEmailTo();

        SimpleMailMessage simpleMessage = new SimpleMailMessage();
        simpleMessage.setTo(emailTo);
        simpleMessage.setFrom(emailFrom);
        simpleMessage.setSubject(subject);
        simpleMessage.setText(messageBody);

        javaMailSender.send(simpleMessage);
    }

    private String getActivationMailBody(String id) {

        String msg = String.format("to complete the activation, click on the link: \n%s",activationServiceUri);

        return msg.replace("{id}", id);
    }
}
