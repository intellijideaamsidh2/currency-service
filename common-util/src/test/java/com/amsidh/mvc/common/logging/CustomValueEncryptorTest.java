package com.amsidh.mvc.common.logging;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for CustomValueEncryptor
 * Demonstrates encryption functionality and pattern matching
 */
class CustomValueEncryptorTest {

    private final CustomValueEncryptor encryptor = new CustomValueEncryptor();

    @Test
    void testJsonPasswordEncryption() {
        String jsonLog = "{\"username\":\"john\",\"password\":\"secret123\",\"email\":\"john@example.com\"}";
        String encrypted = encryptor.encryptSensitiveData(jsonLog);

        assertFalse(encrypted.contains("secret123"));
        // Email should be partially encrypted - first 4 and last 4 chars visible
        assertTrue(encrypted.contains("john")); // First 4 chars should be visible
        assertTrue(encrypted.contains(".com")); // Last 4 chars should be visible
        assertFalse(encrypted.contains("@example")); // Middle part should be encrypted
        // Password should be fully encrypted
        assertFalse(encrypted.contains("\"password\":\"secret123\""));
    }

    @Test
    void testXmlTokenEncryption() {
        String xmlLog = "<user><name>John</name><token>abc123token</token><phone>1234567890</phone></user>";
        String encrypted = encryptor.encryptSensitiveData(xmlLog);

        assertFalse(encrypted.contains("abc123token"));
        assertTrue(encrypted.contains("7890")); // Phone should show last 4 digits
        assertFalse(encrypted.contains("123456")); // First 6 digits should be encrypted
        // Token should be fully encrypted
        assertFalse(encrypted.contains("<token>abc123token</token>"));
    }

    @Test
    void testPartialEncryptionCategories() {
        // Test PARTIAL_LAST_FOUR (phone number)
        String phoneJson = "{\"phone\":\"1234567890\"}";
        String encryptedPhone = encryptor.encryptSensitiveData(phoneJson);
        assertTrue(encryptedPhone.contains("7890")); // Last 4 should be visible
        assertFalse(encryptedPhone.contains("123456")); // First 6 digits should be encrypted

        // Test PARTIAL_FIRST_LAST_FOUR (email)
        String emailJson = "{\"email\":\"john.doe@example.com\"}";
        String encryptedEmail = encryptor.encryptSensitiveData(emailJson);
        assertTrue(encryptedEmail.contains("john")); // First 4 should be visible
        assertTrue(encryptedEmail.contains(".com")); // Last 4 should be visible
        assertFalse(encryptedEmail.contains(".doe@example")); // Middle should be encrypted
        assertFalse(encryptedEmail.contains("john.doe@example.com")); // Full email should not be present
    }

    @Test
    void testFullEncryption() {
        String passwordJson = "{\"password\":\"MySecretPassword123\"}";
        String encrypted = encryptor.encryptSensitiveData(passwordJson);

        assertFalse(encrypted.contains("MySecretPassword123"));
        // Verify no part of the original password is visible
        assertFalse(encrypted.contains("MySecret"));
        assertFalse(encrypted.contains("Password"));
        assertFalse(encrypted.contains("123"));
        // Verify original password field is not present
        assertFalse(encrypted.contains("\"password\":\"MySecretPassword123\""));
        // Should contain encrypted value without wrapper
        assertTrue(encrypted.matches(".*\"password\":\"[A-Za-z0-9+/=]+\".*"));
    }

    @Test
    void testMixedFormatEncryption() {
        String mixedLog = "JSON: {\"password\":\"secret\"} XML: <token>mytoken</token> Header: Authorization: Bearer abc123";
        String encrypted = encryptor.encryptSensitiveData(mixedLog);

        assertFalse(encrypted.contains("secret"));
        assertFalse(encrypted.contains("mytoken"));
        assertFalse(encrypted.contains("Bearer abc123"));

        // Verify original patterns are not present
        assertFalse(encrypted.contains("\"password\":\"secret\""));
        assertFalse(encrypted.contains("<token>mytoken</token>"));
        assertFalse(encrypted.contains("Authorization: Bearer abc123"));
    }

    @Test
    void testEmptyAndNullValues() {
        assertNull(encryptor.encryptSensitiveData(null));
        assertEquals("", encryptor.encryptSensitiveData(""));
        assertEquals("   ", encryptor.encryptSensitiveData("   "));
    }

    @Test
    void testNoSensitiveDataPassthrough() {
        String normalLog = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}";
        String result = encryptor.encryptSensitiveData(normalLog);

        assertEquals(normalLog, result); // Should pass through unchanged
    }

    @Test
    void testCustomPatternAddition() {
        // Add custom pattern for "customSecret" field
        encryptor.addCustomPattern("\"(customSecret)\"\\s*:\\s*\"([^\"]+)\"",
                CustomValueEncryptor.EncryptionCategory.FULL_ENCRYPTED);

        String customLog = "{\"customSecret\":\"myCustomValue\",\"normalField\":\"normalValue\"}";
        String encrypted = encryptor.encryptSensitiveData(customLog);

        assertFalse(encrypted.contains("myCustomValue"));
        assertTrue(encrypted.contains("normalValue")); // Should remain unchanged
        // Verify original pattern is not present
        assertFalse(encrypted.contains("\"customSecret\":\"myCustomValue\""));
    }
}