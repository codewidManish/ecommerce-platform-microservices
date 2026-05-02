package com.ecommerce.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsSimulator {

    public void send(String phoneNumber, String message) {
        // In production: replace with Twilio, AWS SNS, or MSG91
        log.info("\n╔══════════════════════════════════════════╗" +
                 "\n║  SMS SIMULATION                          " +
                 "\n║  To     : {}" +
                 "\n║  Message: {}" +
                 "\n╚══════════════════════════════════════════╝", phoneNumber, message);
    }
}
