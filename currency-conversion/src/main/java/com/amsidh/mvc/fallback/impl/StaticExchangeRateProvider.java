package com.amsidh.mvc.fallback.impl;

import com.amsidh.mvc.fallback.ExchangeRateProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Static exchange rate provider with hardcoded major currency pairs
 * Used as a reliable fallback when other providers are unavailable
 */
@Slf4j
@Component
public class StaticExchangeRateProvider implements ExchangeRateProvider {

    private final Map<String, BigDecimal> staticRates;

    public StaticExchangeRateProvider() {
        staticRates = new HashMap<>();
        initializeStaticRates();
    }

    private void initializeStaticRates() {
        // Major currency pairs - USD as base
        staticRates.put("USD:EUR", new BigDecimal("0.85"));
        staticRates.put("USD:GBP", new BigDecimal("0.75"));
        staticRates.put("USD:JPY", new BigDecimal("110.50"));
        staticRates.put("USD:CAD", new BigDecimal("1.25"));
        staticRates.put("USD:AUD", new BigDecimal("1.35"));
        staticRates.put("USD:CHF", new BigDecimal("0.92"));
        staticRates.put("USD:CNY", new BigDecimal("7.20"));
        staticRates.put("USD:INR", new BigDecimal("83.50"));
        staticRates.put("USD:BRL", new BigDecimal("5.20"));
        staticRates.put("USD:MXN", new BigDecimal("18.50"));

        // EUR as base
        staticRates.put("EUR:USD", new BigDecimal("1.18"));
        staticRates.put("EUR:GBP", new BigDecimal("0.88"));
        staticRates.put("EUR:JPY", new BigDecimal("130.00"));
        staticRates.put("EUR:INR", new BigDecimal("98.24"));

        // GBP as base
        staticRates.put("GBP:USD", new BigDecimal("1.33"));
        staticRates.put("GBP:EUR", new BigDecimal("1.14"));
        staticRates.put("GBP:INR", new BigDecimal("111.33"));

        // INR as base
        staticRates.put("INR:USD", new BigDecimal("0.012"));
        staticRates.put("INR:EUR", new BigDecimal("0.010"));
        staticRates.put("INR:GBP", new BigDecimal("0.009"));

        log.info("Initialized static exchange rates for {} currency pairs", staticRates.size());
    }

    @Override
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }

        String key = createKey(fromCurrency, toCurrency);
        BigDecimal rate = staticRates.get(key);

        if (rate != null) {
            log.info("Retrieved static exchange rate for {}/{}: {}",
                    fromCurrency, toCurrency, rate);
            return rate;
        }

        // Try reverse conversion
        String reverseKey = createKey(toCurrency, fromCurrency);
        BigDecimal reverseRate = staticRates.get(reverseKey);
        if (reverseRate != null && reverseRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal calculatedRate = BigDecimal.ONE.divide(reverseRate, 6, BigDecimal.ROUND_HALF_UP);
            log.info("Calculated reverse exchange rate for {}/{}: {}",
                    fromCurrency, toCurrency, calculatedRate);
            return calculatedRate;
        }

        log.warn("No static exchange rate available for {}/{}", fromCurrency, toCurrency);
        return null;
    }

    @Override
    public String getProviderName() {
        return "STATIC";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getPriority() {
        return 2; // Second priority
    }

    private String createKey(String fromCurrency, String toCurrency) {
        return (fromCurrency + ":" + toCurrency).toUpperCase();
    }

    /**
     * Get all supported currency pairs
     */
    public Map<String, BigDecimal> getSupportedPairs() {
        return new HashMap<>(staticRates);
    }

    /**
     * Check if currency pair is supported
     */
    public boolean isPairSupported(String fromCurrency, String toCurrency) {
        String key = createKey(fromCurrency, toCurrency);
        String reverseKey = createKey(toCurrency, fromCurrency);
        return staticRates.containsKey(key) || staticRates.containsKey(reverseKey);
    }
}