package com.amsidh.mvc.common.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Custom Logback converter that integrates with CustomValueEncryptor
 * to automatically encrypt sensitive data in log messages
 */
public class EncryptedMessageConverter extends ClassicConverter {

    private static final CustomValueEncryptor encryptor = new CustomValueEncryptor();

    @Override
    public String convert(ILoggingEvent event) {
        String originalMessage = event.getFormattedMessage();

        if (originalMessage == null) {
            return "";
        }

        // Apply encryption to sensitive fields
        return encryptor.encryptSensitiveData(originalMessage);
    }
}