package com.amsidh.mvc.common.logging;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for numeric field encryption
 * Demonstrates how numeric values are encrypted while maintaining data type
 */
class NumericEncryptionTest {

    private final CustomValueEncryptor encryptor = new CustomValueEncryptor();

    @Test
    void testNumericPinEncryption() {
        String jsonWithPin = "{\"username\":\"john\",\"pin\":1234,\"balance\":1000.50}";
        String encrypted = encryptor.encryptSensitiveData(jsonWithPin);

        System.out.println("Original: " + jsonWithPin);
        System.out.println("Encrypted: " + encrypted);

        // PIN should be encrypted but remain numeric (no quotes)
        assertFalse(encrypted.contains("\"pin\":1234"));
        assertFalse(encrypted.contains("1234"));

        // Should contain encrypted numeric value without quotes
        assertTrue(encrypted.matches(".*\"pin\":\\d+.*"));

        // Other numeric values should remain unchanged
        assertTrue(encrypted.contains("1000.50"));
        assertTrue(encrypted.contains("\"username\":\"john\""));
    }

    @Test
    void testXmlNumericEncryption() {
        String xmlWithPin = "<user><name>John</name><pin>5678</pin><otp>123456</otp></user>";
        String encrypted = encryptor.encryptSensitiveData(xmlWithPin);

        System.out.println("Original: " + xmlWithPin);
        System.out.println("Encrypted: " + encrypted);

        // PIN and OTP should be encrypted
        assertFalse(encrypted.contains("<pin>5678</pin>"));
        assertFalse(encrypted.contains("<otp>123456</otp>"));
        assertFalse(encrypted.contains("5678"));
        assertFalse(encrypted.contains("123456"));

        // Should contain encrypted numeric values
        assertTrue(encrypted.matches(".*<pin>\\d+</pin>.*"));
        assertTrue(encrypted.matches(".*<otp>\\d+</otp>.*"));
    }

    @Test
    void testMixedNumericAndStringEncryption() {
        String mixed = "{\"password\":\"secret123\",\"pin\":9876,\"phone\":\"1234567890\",\"otp\":456789}";
        String encrypted = encryptor.encryptSensitiveData(mixed);

        System.out.println("Original: " + mixed);
        System.out.println("Encrypted: " + encrypted);

        // String values should be encrypted with quotes
        assertFalse(encrypted.contains("secret123"));
        assertTrue(encrypted.matches(".*\"password\":\"[A-Za-z0-9+/=]+\".*"));

        // Phone (string) should show last 4 digits
        assertTrue(encrypted.contains("7890"));

        // Numeric values should be encrypted without quotes
        assertFalse(encrypted.contains("9876"));
        assertFalse(encrypted.contains("456789"));
        assertTrue(encrypted.matches(".*\"pin\":\\d+.*"));
        assertTrue(encrypted.matches(".*\"otp\":\\d+.*"));
    }

    @Test
    void testNumericDataTypePreservation() {
        String original = "{\"pin\":1234}";
        String encrypted = encryptor.encryptSensitiveData(original);

        System.out.println("Data Type Test:");
        System.out.println("Original: " + original);
        System.out.println("Encrypted: " + encrypted);

        // Verify the encrypted value is still a valid JSON number (no quotes)
        assertFalse(encrypted.contains("\"pin\":\""));
        assertTrue(encrypted.matches(".*\"pin\":\\d+.*"));

        // Parse as JSON to verify it's still valid
        assertTrue(encrypted.startsWith("{"));
        assertTrue(encrypted.endsWith("}"));
        assertFalse(encrypted.contains("1234")); // Original value should be gone
    }

    @Test
    void testLargeNumericValues() {
        String largeNumbers = "{\"creditCard\":1234567890123456,\"accountNumber\":9876543210}";
        String encrypted = encryptor.encryptSensitiveData(largeNumbers);

        System.out.println("Large Numbers Test:");
        System.out.println("Original: " + largeNumbers);
        System.out.println("Encrypted: " + encrypted);

        // Original values should be encrypted
        assertFalse(encrypted.contains("1234567890123456"));
        assertFalse(encrypted.contains("9876543210"));

        // Should still be numeric (no quotes)
        assertTrue(encrypted.matches(".*\"creditCard\":\\d+.*"));
        assertTrue(encrypted.matches(".*\"accountNumber\":\\d+.*"));
    }
}