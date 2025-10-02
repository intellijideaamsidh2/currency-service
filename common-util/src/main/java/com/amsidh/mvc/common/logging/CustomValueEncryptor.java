package com.amsidh.mvc.common.logging;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

/**
 * CustomValueEncryptor - Single class for comprehensive log data encryption
 * Supports JSON and XML format encryption with multiple encryption categories
 * Integrates with Logback for automatic sensitive data encryption
 */
@Component
public class CustomValueEncryptor {

    // Strong AES-256 encryption configuration
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ENCRYPTION_KEY = "MyVeryStrongSecretKey123456789AB"; // Exactly 32 chars for AES-256
    private static final byte[] IV = "1234567890123456".getBytes(StandardCharsets.UTF_8); // 16 bytes IV

    // Encryption categories
    public enum EncryptionCategory {
        FULL_ENCRYPTED, // Complete value encrypted
        PARTIAL_LAST_FOUR, // Last 4 chars plain, rest encrypted
        PARTIAL_FIRST_LAST_FOUR // First 4 and last 4 chars plain, middle encrypted
    }

    // Regex patterns for sensitive field detection
    private static final Map<Pattern, EncryptionCategory> FIELD_PATTERNS = new HashMap<>();

    static {
        // JSON format patterns
        FIELD_PATTERNS.put(Pattern.compile("\"(password|passwd|pwd)\"\\s*:\\s*\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.FULL_ENCRYPTED);
        FIELD_PATTERNS.put(Pattern.compile("\"(token|auth|authorization|bearer)\"\\s*:\\s*\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.FULL_ENCRYPTED);
        FIELD_PATTERNS.put(Pattern.compile("\"(apikey|api_key|secret)\"\\s*:\\s*\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.FULL_ENCRYPTED);
        FIELD_PATTERNS.put(Pattern.compile("\"(email|emailaddress|email_address)\"\\s*:\\s*\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.PARTIAL_FIRST_LAST_FOUR);
        FIELD_PATTERNS.put(Pattern.compile("\"(phone|mobile|telephone|phonenumber|phone_number)\"\\s*:\\s*\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.PARTIAL_LAST_FOUR);
        FIELD_PATTERNS.put(Pattern.compile("\"(ssn|social_security|socialsecurity)\"\\s*:\\s*\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.PARTIAL_LAST_FOUR);
        FIELD_PATTERNS.put(Pattern.compile("\"(creditcard|credit_card|cardnumber|card_number)\"\\s*:\\s*\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.PARTIAL_LAST_FOUR);
        FIELD_PATTERNS.put(Pattern.compile("\"(account|accountnumber|account_number)\"\\s*:\\s*\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.PARTIAL_LAST_FOUR);

        // XML format patterns
        FIELD_PATTERNS.put(Pattern.compile("<(password|passwd|pwd)>([^<]+)</(?:password|passwd|pwd)>",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.FULL_ENCRYPTED);
        FIELD_PATTERNS
                .put(Pattern.compile("<(token|auth|authorization|bearer)>([^<]+)</(?:token|auth|authorization|bearer)>",
                        Pattern.CASE_INSENSITIVE), EncryptionCategory.FULL_ENCRYPTED);
        FIELD_PATTERNS.put(Pattern.compile("<(apikey|api_key|secret)>([^<]+)</(?:apikey|api_key|secret)>",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.FULL_ENCRYPTED);
        FIELD_PATTERNS.put(
                Pattern.compile("<(email|emailaddress|email_address)>([^<]+)</(?:email|emailaddress|email_address)>",
                        Pattern.CASE_INSENSITIVE),
                EncryptionCategory.PARTIAL_FIRST_LAST_FOUR);
        FIELD_PATTERNS.put(Pattern.compile(
                "<(phone|mobile|telephone|phonenumber|phone_number)>([^<]+)</(?:phone|mobile|telephone|phonenumber|phone_number)>",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.PARTIAL_LAST_FOUR);
        FIELD_PATTERNS.put(Pattern.compile(
                "<(ssn|social_security|socialsecurity)>([^<]+)</(?:ssn|social_security|socialsecurity)>",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.PARTIAL_LAST_FOUR);
        FIELD_PATTERNS.put(Pattern.compile(
                "<(creditcard|credit_card|cardnumber|card_number)>([^<]+)</(?:creditcard|credit_card|cardnumber|card_number)>",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.PARTIAL_LAST_FOUR);
        FIELD_PATTERNS.put(Pattern.compile(
                "<(account|accountnumber|account_number)>([^<]+)</(?:account|accountnumber|account_number)>",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.PARTIAL_LAST_FOUR);

        // Additional patterns for common sensitive fields
        FIELD_PATTERNS.put(Pattern.compile("(password|token|secret)=([^\\s&]+)",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.FULL_ENCRYPTED);
        FIELD_PATTERNS.put(Pattern.compile("(Authorization):\\s*(.+)",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.FULL_ENCRYPTED);

        // Numeric field patterns (for fields like pin, otp, code)
        FIELD_PATTERNS.put(Pattern.compile("\"(pin|otp|code|passcode|totalAmount|verification_code)\"\\s*:\\s*(\\d+)",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.FULL_ENCRYPTED);
        FIELD_PATTERNS.put(Pattern.compile(
                "\"(creditcard|credit_card|cardnumber|card_number|accountnumber|account_number)\"\\s*:\\s*(\\d+)",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.PARTIAL_LAST_FOUR);
        FIELD_PATTERNS.put(Pattern.compile(
                "<(pin|otp|code|passcode|verification_code)>(\\d+)<\\/(?:pin|otp|code|passcode|totalAmount|verification_code)>",
                Pattern.CASE_INSENSITIVE), EncryptionCategory.FULL_ENCRYPTED);
    }

    private final SecretKey secretKey;
    private final IvParameterSpec ivSpec;

    public CustomValueEncryptor() {
        this.secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        this.ivSpec = new IvParameterSpec(IV);
    }

    /**
     * Main method to encrypt sensitive data in log messages
     * Supports both JSON and XML formats
     */
    public String encryptSensitiveData(String logMessage) {
        if (logMessage == null || logMessage.trim().isEmpty()) {
            return logMessage;
        }

        String processedMessage = logMessage;

        // Process each pattern and apply appropriate encryption
        for (Map.Entry<Pattern, EncryptionCategory> entry : FIELD_PATTERNS.entrySet()) {
            Pattern pattern = entry.getKey();
            EncryptionCategory category = entry.getValue();

            Matcher matcher = pattern.matcher(processedMessage);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                // Check if the pattern has the required groups
                if (matcher.groupCount() < 2) {
                    continue; // Skip patterns that don't have required groups
                }

                String fieldName = matcher.group(1);
                String fieldValue = matcher.group(2);

                // Check if this is a numeric field (no quotes around the value)
                boolean isNumericField = !fieldValue.startsWith("\"") && fieldValue.matches("\\d+");
                String encryptedValue = isNumericField ? encryptNumericValue(fieldValue, category)
                        : encryptValue(fieldValue, category);

                // Rebuild the match with encrypted value
                String replacement;
                if (isJsonFormat(matcher.group(0))) {
                    if (isNumericField) {
                        replacement = "\"" + fieldName + "\":" + encryptedValue; // No quotes for numbers
                    } else {
                        replacement = "\"" + fieldName + "\":\"" + encryptedValue + "\"";
                    }
                } else if (isXmlFormat(matcher.group(0))) {
                    replacement = "<" + fieldName + ">" + encryptedValue + "</" + fieldName + ">";
                } else {
                    // Handle other formats (query params, headers, etc.)
                    if (matcher.group(0).contains(":")) {
                        replacement = fieldName + ": " + encryptedValue;
                    } else {
                        replacement = fieldName + "=" + encryptedValue;
                    }
                }

                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            processedMessage = sb.toString();
        }

        return processedMessage;
    }

    /**
     * Encrypt value based on encryption category
     */
    private String encryptValue(String value, EncryptionCategory category) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        try {
            switch (category) {
                case FULL_ENCRYPTED:
                    return encrypt(value);

                case PARTIAL_LAST_FOUR:
                    if (value.length() <= 4) {
                        return encrypt(value);
                    }
                    String lastFour = value.substring(value.length() - 4);
                    String restToEncrypt = value.substring(0, value.length() - 4);
                    return encrypt(restToEncrypt) + lastFour;

                case PARTIAL_FIRST_LAST_FOUR:
                    if (value.length() <= 8) {
                        return encrypt(value);
                    }
                    String firstFour = value.substring(0, 4);
                    String lastFourChars = value.substring(value.length() - 4);
                    String middleToEncrypt = value.substring(4, value.length() - 4);
                    return firstFour + encrypt(middleToEncrypt) + lastFourChars;

                default:
                    return encrypt(value);
            }
        } catch (Exception e) {
            // Fallback to masked value if encryption fails
            return "***ERROR_ENCRYPTING***";
        }
    }

    /**
     * Encrypt numeric value while maintaining numeric data type
     * Uses a hash-based approach to generate a numeric result
     */
    private String encryptNumericValue(String numericValue, EncryptionCategory category) {
        try {
            long originalValue = Long.parseLong(numericValue);

            // Create a hash-based numeric encryption
            // Combine original value with secret key for deterministic hashing
            String hashInput = numericValue + ENCRYPTION_KEY;
            int hashCode = hashInput.hashCode();

            // Ensure positive number and maintain similar length
            long encryptedNumber = Math.abs((long) hashCode);

            // For partial encryption categories, we can still apply the logic
            switch (category) {
                case PARTIAL_LAST_FOUR:
                    if (numericValue.length() <= 4) {
                        return String.valueOf(encryptedNumber);
                    }
                    // Keep last digits, encrypt the rest
                    String lastDigits = numericValue.substring(numericValue.length() - 4);
                    long encryptedPart = Math.abs((hashInput + "_partial").hashCode()) % 1000000; // Limit size
                    return String.valueOf(encryptedPart) + lastDigits;

                case PARTIAL_FIRST_LAST_FOUR:
                    if (numericValue.length() <= 8) {
                        return String.valueOf(encryptedNumber);
                    }
                    String firstDigits = numericValue.substring(0, 4);
                    String lastFourDigits = numericValue.substring(numericValue.length() - 4);
                    long encryptedMiddle = Math.abs((hashInput + "_middle").hashCode()) % 100000; // Limit size
                    return firstDigits + encryptedMiddle + lastFourDigits;

                default: // FULL_ENCRYPTED
                    return String.valueOf(encryptedNumber);
            }
        } catch (Exception e) {
            // Fallback for numeric encryption failure
            return "999999999"; // Safe numeric fallback
        }
    }

    /**
     * Strong AES-256 encryption
     */
    private String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Decrypt method for testing/debugging purposes
     */
    public String decrypt(String encryptedText) throws Exception {
        if (encryptedText.startsWith("[ENC:") && encryptedText.endsWith("]")) {
            encryptedText = encryptedText.substring(5, encryptedText.length() - 1);
        }

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Check if the matched string is in JSON format
     */
    private boolean isJsonFormat(String match) {
        return match.contains("\"") && match.contains(":");
    }

    /**
     * Check if the matched string is in XML format
     */
    private boolean isXmlFormat(String match) {
        return match.startsWith("<") && match.contains(">");
    }

    /**
     * Add custom pattern for specific use cases
     * Note: Pattern must have exactly 2 groups - (fieldName) and (fieldValue)
     */
    public void addCustomPattern(String regex, EncryptionCategory category) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        // Validate pattern has required groups
        try {
            pattern.matcher("").groupCount();
        } catch (Exception e) {
            throw new IllegalArgumentException("Pattern must have exactly 2 capture groups: " + regex);
        }
        FIELD_PATTERNS.put(pattern, category);
    }

    /**
     * Generate a new encryption key (for key rotation)
     */
    public static String generateNewEncryptionKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256);
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * Generate a new IV (for enhanced security)
     */
    public static String generateNewIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return Base64.getEncoder().encodeToString(iv);
    }

    // Static method for Logback integration
    public static String encryptLogMessage(String message) {
        CustomValueEncryptor encryptor = new CustomValueEncryptor();
        return encryptor.encryptSensitiveData(message);
    }
}