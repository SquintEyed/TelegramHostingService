package org.example.service.impl;

import lombok.extern.log4j.Log4j;
import org.example.service.ProducerService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import static org.example.model.RabbitQueue.*;

@Service
@Log4j
public class ProduceServiceImpl implements ProducerService {

    private final RabbitTemplate rabbitTemplate;

    public ProduceServiceImpl(RabbitTemplate rabbitTemplate) {
         this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void produceAnswer(SendMessage sendMessage) {

        rabbitTemplate.convertAndSend(ANSWER_MESSAGE, sendMessage);
    }
}
