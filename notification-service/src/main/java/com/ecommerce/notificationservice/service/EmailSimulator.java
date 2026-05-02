package com.ecommerce.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailSimulator {

    public void send(String to, String subject, String body) {
        // In production: replace with JavaMailSender, SendGrid, or AWS SES
        log.info("\n╔══════════════════════════════════════════╗" +
                 "\n║  EMAIL SIMULATION                        " +
                 "\n║  To     : {}" +
                 "\n║  Subject: {}" +
                 "\n║  Body   : {}" +
                 "\n╚══════════════════════════════════════════╝", to, subject, body);
    }
}
