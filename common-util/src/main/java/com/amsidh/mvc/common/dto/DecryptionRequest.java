package com.amsidh.mvc.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for decryption operations
 */
public class DecryptionRequest {

    @JsonProperty("encrypted_value")
    private String encryptedValue;

    @JsonProperty("original_length")
    private Integer originalLength;

    @JsonProperty("type")
    private String type; // "string", "numeric", "log"

    public DecryptionRequest() {
    }

    public DecryptionRequest(String encryptedValue) {
        this.encryptedValue = encryptedValue;
    }

    public DecryptionRequest(String encryptedValue, String type) {
        this.encryptedValue = encryptedValue;
        this.type = type;
    }

    // Getters and Setters
    public String getEncryptedValue() {
        return encryptedValue;
    }

    public void setEncryptedValue(String encryptedValue) {
        this.encryptedValue = encryptedValue;
    }

    public Integer getOriginalLength() {
        return originalLength;
    }

    public void setOriginalLength(Integer originalLength) {
        this.originalLength = originalLength;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "DecryptionRequest{" +
                "encryptedValue='" + (encryptedValue != null ? "[ENCRYPTED]" : null) + '\'' +
                ", originalLength=" + originalLength +
                ", type='" + type + '\'' +
                '}';
    }
}