package com.amsidh.mvc.common.logging;

import org.junit.jupiter.api.Test;

/**
 * Comprehensive demo of the CustomValueEncryptor functionality
 * Shows how encryption works seamlessly without [ENC:] wrapper
 */
class EncryptionDemoTest {

    private final CustomValueEncryptor encryptor = new CustomValueEncryptor();

    @Test
    void demonstrateAllEncryptionTypes() {
        System.out.println("=== CustomValueEncryptor Demonstration ===\n");

        // 1. Full Encryption Examples
        System.out.println("1. FULL ENCRYPTION (passwords, tokens, secrets):");
        String password = "{\"password\":\"MySecretPassword123\"}";
        String passwordEncrypted = encryptor.encryptSensitiveData(password);
        System.out.println("Original: " + password);
        System.out.println("Encrypted: " + passwordEncrypted + "\n");

        String token = "<token>super-secret-api-token-12345</token>";
        String tokenEncrypted = encryptor.encryptSensitiveData(token);
        System.out.println("Original: " + token);
        System.out.println("Encrypted: " + tokenEncrypted + "\n");

        // 2. Partial Encryption - Last 4 chars visible
        System.out.println("2. PARTIAL ENCRYPTION - Last 4 chars visible (phone, SSN, credit card):");
        String phone = "{\"phone\":\"1234567890\"}";
        String phoneEncrypted = encryptor.encryptSensitiveData(phone);
        System.out.println("Original: " + phone);
        System.out.println("Encrypted: " + phoneEncrypted + " (shows last 4: 7890)\n");

        String ssn = "<ssn>123456789</ssn>";
        String ssnEncrypted = encryptor.encryptSensitiveData(ssn);
        System.out.println("Original: " + ssn);
        System.out.println("Encrypted: " + ssnEncrypted + " (shows last 4: 6789)\n");

        // 3. Partial Encryption - First 4 and Last 4 chars visible
        System.out.println("3. PARTIAL ENCRYPTION - First 4 and Last 4 chars visible (email):");
        String email = "{\"email\":\"amsidhlokhande@gmail.com\"}";
        String emailEncrypted = encryptor.encryptSensitiveData(email);
        System.out.println("Original: " + email);
        System.out.println("Encrypted: " + emailEncrypted);
        System.out.println("Notice: 'amsi' + encrypted_middle + '.com'\n");

        String longEmail = "{\"email\":\"john.doe.smith@example.com\"}";
        String longEmailEncrypted = encryptor.encryptSensitiveData(longEmail);
        System.out.println("Original: " + longEmail);
        System.out.println("Encrypted: " + longEmailEncrypted);
        System.out.println("Notice: 'john' + encrypted_middle + '.com'\n");

        // 4. Mixed format in single log message
        System.out.println("4. MIXED FORMAT ENCRYPTION:");
        String mixed = "User login: {\"username\":\"john\",\"password\":\"secret123\",\"email\":\"user@example.com\"} "
                +
                "with header Authorization: Bearer token123 and phone <phone>9876543210</phone>";
        String mixedEncrypted = encryptor.encryptSensitiveData(mixed);
        System.out.println("Original: " + mixed);
        System.out.println("Encrypted: " + mixedEncrypted + "\n");

        // 5. Custom pattern addition
        System.out.println("5. CUSTOM PATTERN ADDITION:");
        encryptor.addCustomPattern("\"(customSecret)\"\\s*:\\s*\"([^\"]+)\"",
                CustomValueEncryptor.EncryptionCategory.FULL_ENCRYPTED);

        String custom = "{\"customSecret\":\"mySpecialValue\",\"publicInfo\":\"this stays visible\"}";
        String customEncrypted = encryptor.encryptSensitiveData(custom);
        System.out.println("Original: " + custom);
        System.out.println("Encrypted: " + customEncrypted + "\n");

        System.out.println("=== Key Benefits ===");
        System.out.println("✓ No [ENC:] wrapper - seamless integration");
        System.out.println("✓ Three encryption categories: full, partial-last-4, partial-first-last-4");
        System.out.println("✓ Supports JSON, XML, and custom formats");
        System.out.println("✓ Strong AES-256 encryption");
        System.out.println("✓ Configurable patterns via regex");
        System.out.println("✓ Production-ready with error handling");
    }
}