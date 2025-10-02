package com.amsidh.mvc.fallback.impl;

import com.amsidh.mvc.fallback.ExchangeRateProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Default exchange rate provider that always returns 1:1 ratio
 * Used as the ultimate fallback when all other providers fail
 */
@Slf4j
@Component
public class DefaultExchangeRateProvider implements ExchangeRateProvider {

    @Override
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }

        log.warn("Using default 1:1 exchange rate for {}/{} - all other providers unavailable",
                fromCurrency, toCurrency);
        return BigDecimal.ONE;
    }

    @Override
    public String getProviderName() {
        return "DEFAULT";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getPriority() {
        return 3; // Lowest priority - last resort
    }
}