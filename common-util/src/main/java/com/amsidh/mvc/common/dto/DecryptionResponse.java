package com.amsidh.mvc.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for decryption operations
 */
public class DecryptionResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("decrypted_value")
    private String decryptedValue;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("timestamp")
    private long timestamp;

    public DecryptionResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public DecryptionResponse(boolean success, String decryptedValue, String errorMessage) {
        this();
        this.success = success;
        this.decryptedValue = decryptedValue;
        this.errorMessage = errorMessage;
    }

    // Static factory methods
    public static DecryptionResponse success(String decryptedValue) {
        return new DecryptionResponse(true, decryptedValue, null);
    }

    public static DecryptionResponse error(String errorMessage) {
        return new DecryptionResponse(false, null, errorMessage);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getDecryptedValue() {
        return decryptedValue;
    }

    public void setDecryptedValue(String decryptedValue) {
        this.decryptedValue = decryptedValue;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "DecryptionResponse{" +
                "success=" + success +
                ", decryptedValue='" + (decryptedValue != null ? "[DECRYPTED]" : null) + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}