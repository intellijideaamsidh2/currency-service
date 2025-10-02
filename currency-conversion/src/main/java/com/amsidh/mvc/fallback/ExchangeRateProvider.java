package com.amsidh.mvc.fallback;

import java.math.BigDecimal;

/**
 * Interface for different exchange rate providers used in fallback scenarios
 */
public interface ExchangeRateProvider {

    /**
     * Get exchange rate from one currency to another
     * 
     * @param fromCurrency source currency code
     * @param toCurrency   target currency code
     * @return exchange rate or null if not available
     */
    BigDecimal getExchangeRate(String fromCurrency, String toCurrency);

    /**
     * Get provider name for logging and monitoring
     * 
     * @return provider name
     */
    String getProviderName();

    /**
     * Check if provider is available
     * 
     * @return true if provider can be used
     */
    boolean isAvailable();

    /**
     * Get priority order (lower number = higher priority)
     * 
     * @return priority order
     */
    int getPriority();
}