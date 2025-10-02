package com.amsidh.mvc.fallback.impl;

import com.amsidh.mvc.fallback.ExchangeRateProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache-based exchange rate provider
 * Stores recently successful exchange rates for fallback scenarios
 */
@Slf4j
@Component
public class CachedExchangeRateProvider implements ExchangeRateProvider {

    private final ConcurrentHashMap<String, CachedRate> rateCache = new ConcurrentHashMap<>();
    private static final int CACHE_EXPIRY_HOURS = 2;

    @Override
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        String key = createKey(fromCurrency, toCurrency);
        CachedRate cachedRate = rateCache.get(key);

        if (cachedRate != null && !cachedRate.isExpired()) {
            log.info("Retrieved cached exchange rate for {}/{}: {}",
                    fromCurrency, toCurrency, cachedRate.getRate());
            return cachedRate.getRate();
        }

        // Remove expired entries
        if (cachedRate != null && cachedRate.isExpired()) {
            rateCache.remove(key);
            log.debug("Removed expired cached rate for {}/{}", fromCurrency, toCurrency);
        }

        return null;
    }

    /**
     * Cache a successful exchange rate
     */
    public void cacheExchangeRate(String fromCurrency, String toCurrency, BigDecimal rate) {
        if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
            String key = createKey(fromCurrency, toCurrency);
            CachedRate cachedRate = new CachedRate(rate, LocalDateTime.now().plusHours(CACHE_EXPIRY_HOURS));
            rateCache.put(key, cachedRate);
            log.debug("Cached exchange rate for {}/{}: {}", fromCurrency, toCurrency, rate);
        }
    }

    @Override
    public String getProviderName() {
        return "CACHED";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getPriority() {
        return 1; // Highest priority
    }

    private String createKey(String fromCurrency, String toCurrency) {
        return (fromCurrency + ":" + toCurrency).toUpperCase();
    }

    /**
     * Get cache statistics for monitoring
     */
    public CacheStats getCacheStats() {
        int totalEntries = rateCache.size();
        long expiredEntries = rateCache.values().stream()
                .mapToLong(rate -> rate.isExpired() ? 1 : 0)
                .sum();

        return new CacheStats(totalEntries, (int) expiredEntries);
    }

    /**
     * Clear expired entries from cache
     */
    public void cleanupExpiredEntries() {
        rateCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    // Inner classes
    private static class CachedRate {
        private final BigDecimal rate;
        private final LocalDateTime expiryTime;

        public CachedRate(BigDecimal rate, LocalDateTime expiryTime) {
            this.rate = rate;
            this.expiryTime = expiryTime;
        }

        public BigDecimal getRate() {
            return rate;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    public static class CacheStats {
        private final int totalEntries;
        private final int expiredEntries;

        public CacheStats(int totalEntries, int expiredEntries) {
            this.totalEntries = totalEntries;
            this.expiredEntries = expiredEntries;
        }

        public int getTotalEntries() {
            return totalEntries;
        }

        public int getExpiredEntries() {
            return expiredEntries;
        }

        public int getActiveEntries() {
            return totalEntries - expiredEntries;
        }
    }
}