package com.amsidh.mvc.common.util;

import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Utility class for common operations
 */
public final class CommonUtils {

    private static final String HOSTNAME = getHostname();
    private static final String INSTANCE_ID = getInstanceId();

    private CommonUtils() {
        // Utility class
    }

    /**
     * Get current hostname
     */
    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown-host";
        }
    }

    /**
     * Get instance ID (hostname or container ID)
     */
    public static String getInstanceId() {
        String hostname = System.getenv("HOSTNAME");
        if (StringUtils.hasText(hostname)) {
            return hostname;
        }

        String containerId = System.getenv("CONTAINER_ID");
        if (StringUtils.hasText(containerId)) {
            return containerId;
        }

        return HOSTNAME;
    }

    /**
     * Generate correlation ID
     */
    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Get current timestamp in ISO format
     */
    public static String getCurrentTimestamp() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }

    /**
     * Mask sensitive information
     */
    public static String maskString(String input, int visibleChars) {
        if (!StringUtils.hasText(input)) {
            return input;
        }

        if (input.length() <= visibleChars) {
            return "*".repeat(input.length());
        }

        String visible = input.substring(0, visibleChars);
        String masked = "*".repeat(input.length() - visibleChars);
        return visible + masked;
    }

    /**
     * Get cached hostname
     */
    public static String getCachedHostname() {
        return HOSTNAME;
    }

    /**
     * Get cached instance ID
     */
    public static String getCachedInstanceId() {
        return INSTANCE_ID;
    }

    /**
     * Get service environment information including hostname and port
     * Essential for Kubernetes pod identification and debugging
     */
    public static String getServiceEnvironmentInfo(String serviceName, String port) {
        if (!StringUtils.hasText(serviceName)) {
            serviceName = "unknown-service";
        }
        if (!StringUtils.hasText(port)) {
            port = "unknown";
        }

        return String.format("%s [hostname: %s, port: %s]", serviceName, getCachedHostname(), port);
    }

    /**
     * Get service environment information with additional suffix
     * Useful for fallback scenarios or additional context
     */
    public static String getServiceEnvironmentInfo(String serviceName, String port, String suffix) {
        String baseInfo = getServiceEnvironmentInfo(serviceName, port);
        if (StringUtils.hasText(suffix)) {
            return baseInfo + " [" + suffix + "]";
        }
        return baseInfo;
    }
}