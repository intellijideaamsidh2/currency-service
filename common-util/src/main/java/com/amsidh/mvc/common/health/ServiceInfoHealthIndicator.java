package com.amsidh.mvc.common.health;

import com.amsidh.mvc.common.util.CommonUtils;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom health indicator with service information
 */
@Component
public class ServiceInfoHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        details.put("hostname", CommonUtils.getCachedHostname());
        details.put("instance_id", CommonUtils.getCachedInstanceId());
        details.put("timestamp", Instant.now().toString());
        details.put("uptime_ms", getUptime());

        return Health.up()
                .withDetails(details)
                .build();
    }

    private long getUptime() {
        return System.currentTimeMillis() - getStartTime();
    }

    private long getStartTime() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime();
    }
}