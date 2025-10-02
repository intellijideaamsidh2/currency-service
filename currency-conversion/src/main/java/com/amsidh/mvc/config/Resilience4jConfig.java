package com.amsidh.mvc.config;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4jConfig {

    private static final String CURRENCY_EXCHANGE_INSTANCE = "currency-exchange";

    @Bean
    public RateLimiter currencyExchangeRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(2) // Even more restrictive - only 2 requests per period
                .limitRefreshPeriod(Duration.ofSeconds(10)) // Shorter refresh period
                .timeoutDuration(Duration.ofMillis(1)) // Extremely short timeout - requests will fail immediately
                .build();

        return RateLimiter.of(CURRENCY_EXCHANGE_INSTANCE, config);
    }

    @Bean
    public CircuitBreaker currencyExchangeCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .build();

        return CircuitBreaker.of(CURRENCY_EXCHANGE_INSTANCE, config);
    }

    @Bean
    public Retry currencyExchangeRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .build();

        return Retry.of(CURRENCY_EXCHANGE_INSTANCE, config);
    }

    @Bean
    public Bulkhead currencyExchangeBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(5)
                .maxWaitDuration(Duration.ofSeconds(1))
                .build();

        return Bulkhead.of(CURRENCY_EXCHANGE_INSTANCE, config);
    }
}