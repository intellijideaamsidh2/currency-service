package com.amsidh.mvc.service;

import com.amsidh.mvc.fallback.ExchangeRateProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service that orchestrates multiple fallback providers for exchange rates
 * Implements priority-based fallback with circuit breaker protection
 */
@Slf4j
@Service
public class FallbackOrchestrationService {

    private final List<ExchangeRateProvider> exchangeRateProviders;

    @Autowired
    public FallbackOrchestrationService(List<ExchangeRateProvider> exchangeRateProviders) {
        this.exchangeRateProviders = exchangeRateProviders.stream()
                .sorted((p1, p2) -> Integer.compare(p1.getPriority(), p2.getPriority()))
                .collect(Collectors.toList());

        log.info("Initialized fallback orchestration with {} providers: {}",
                this.exchangeRateProviders.size(),
                this.exchangeRateProviders.stream()
                        .map(p -> p.getProviderName() + "(priority:" + p.getPriority() + ")")
                        .collect(Collectors.joining(", ")));
    }

    /**
     * Get exchange rate using priority-based fallback mechanism
     * 
     * @param fromCurrency Source currency code
     * @param toCurrency   Target currency code
     * @return Exchange rate or default 1:1 if all providers fail
     */
    public BigDecimal getExchangeRateWithFallback(String fromCurrency, String toCurrency) {
        log.debug("Attempting to get exchange rate for {}/{} using {} providers",
                fromCurrency, toCurrency, exchangeRateProviders.size());

        for (ExchangeRateProvider provider : exchangeRateProviders) {
            try {
                if (provider.isAvailable()) {
                    BigDecimal rate = provider.getExchangeRate(fromCurrency, toCurrency);
                    if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
                        log.debug("Successfully retrieved exchange rate {} from provider {}",
                                rate, provider.getProviderName());
                        return rate;
                    }
                }
                log.debug("Provider {} is not available or returned invalid rate",
                        provider.getProviderName());
            } catch (Exception e) {
                log.warn("Provider {} failed to provide exchange rate: {}",
                        provider.getProviderName(), e.getMessage());
            }
        }

        log.error("All exchange rate providers failed for {}/{}, returning default rate",
                fromCurrency, toCurrency);
        return BigDecimal.ONE;
    }

    /**
     * Get status of all providers for health checks
     */
    public List<ProviderStatus> getProviderStatuses() {
        return exchangeRateProviders.stream()
                .map(provider -> new ProviderStatus(
                        provider.getProviderName(),
                        provider.getPriority(),
                        provider.isAvailable()))
                .collect(Collectors.toList());
    }

    /**
     * Provider status for monitoring
     */
    public static class ProviderStatus {
        private final String name;
        private final int priority;
        private final boolean available;

        public ProviderStatus(String name, int priority, boolean available) {
            this.name = name;
            this.priority = priority;
            this.available = available;
        }

        public String getName() {
            return name;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isAvailable() {
            return available;
        }

        @Override
        public String toString() {
            return String.format("Provider{name='%s', priority=%d, available=%s}",
                    name, priority, available);
        }
    }
}