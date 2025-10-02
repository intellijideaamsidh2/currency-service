package com.amsidh.mvc.service.impl;

import com.amsidh.mvc.client.CurrencyExchangeClient;
import com.amsidh.mvc.common.util.CommonUtils;
import com.amsidh.mvc.dto.CurrencyConversionDto;
import com.amsidh.mvc.dto.CurrencyExchangeDto;
import com.amsidh.mvc.service.CurrencyConversionService;
import com.amsidh.mvc.service.FallbackOrchestrationService;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyConversionServiceImpl implements CurrencyConversionService {

    private final CurrencyExchangeClient currencyExchangeClient;
    private final FallbackOrchestrationService fallbackOrchestrationService;
    private final RateLimiter rateLimiter;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final Bulkhead bulkhead;

    @Value("${server.port:8282}")
    private String serverPort;

    @Override
    public CurrencyConversionDto convertCurrency(String fromCurrency, String toCurrency,
            BigDecimal quantity) {
        log.debug("Converting currency: {} to {} with quantity {}", fromCurrency, toCurrency, quantity);

        try {
            // Apply Resilience4j patterns programmatically
            return rateLimiter.executeSupplier(() -> bulkhead
                    .executeSupplier(() -> circuitBreaker.executeSupplier(() -> retry.executeSupplier(() -> {
                        // Call currency-exchange service to get the rate
                        CurrencyExchangeDto exchangeRate = currencyExchangeClient.getExchangeRate(fromCurrency,
                                toCurrency);

                        // Calculate total amount
                        BigDecimal totalAmount = exchangeRate.getRate().multiply(quantity);

                        log.debug("Successfully retrieved exchange rate {} from currency-exchange service",
                                exchangeRate.getRate());

                        // Build and return the conversion response
                        return CurrencyConversionDto.builder()
                                .fromCurrency(fromCurrency)
                                .toCurrency(toCurrency)
                                .quantity(quantity)
                                .rate(exchangeRate.getRate())
                                .totalAmount(totalAmount)
                                .currencyExchangeServiceEnvironment(exchangeRate.getEnvironment())
                                .currencyConversionServiceEnvironment(getServiceEnvironmentInfo())
                                .build();
                    }))));
        } catch (Exception ex) {
            // Fallback logic when all resilience patterns fail
            return convertCurrencyFallback(fromCurrency, toCurrency, quantity, ex);
        }
    }

    /**
     * Fallback method for currency conversion when all resilience patterns fail
     * Uses fallback orchestration service to get exchange rates from multiple
     * providers
     */
    public CurrencyConversionDto convertCurrencyFallback(String fromCurrency, String toCurrency,
            BigDecimal quantity, Exception ex) {
        log.warn("Currency conversion fallback triggered for {}/{} due to: {}",
                fromCurrency, toCurrency, ex.getMessage());

        try {
            // Use fallback orchestration service to get exchange rate
            BigDecimal fallbackRate = fallbackOrchestrationService.getExchangeRateWithFallback(fromCurrency,
                    toCurrency);

            // Calculate total amount
            BigDecimal totalAmount = fallbackRate.multiply(quantity);

            log.info("Successfully retrieved fallback exchange rate {} for {}/{}",
                    fallbackRate, fromCurrency, toCurrency);

            // Build and return the conversion response with fallback indicator
            CurrencyConversionDto result = CurrencyConversionDto.builder()
                    .fromCurrency(fromCurrency)
                    .toCurrency(toCurrency)
                    .quantity(quantity)
                    .rate(fallbackRate)
                    .totalAmount(totalAmount)
                    .currencyExchangeServiceEnvironment(
                            "FALLBACK: Multiple providers used due to service unavailability")
                    .currencyConversionServiceEnvironment(
                            getServiceEnvironmentInfo() + " [RESILIENCE4J-FALLBACK]")
                    .build();

            return result;
        } catch (Exception fallbackEx) {
            log.error("Fallback also failed for {}/{}: {}", fromCurrency, toCurrency, fallbackEx.getMessage());

            // Ultimate fallback - return 1:1 rate
            BigDecimal ultimateRate = BigDecimal.ONE;
            BigDecimal totalAmount = ultimateRate.multiply(quantity);

            CurrencyConversionDto result = CurrencyConversionDto.builder()
                    .fromCurrency(fromCurrency)
                    .toCurrency(toCurrency)
                    .quantity(quantity)
                    .rate(ultimateRate)
                    .totalAmount(totalAmount)
                    .currencyExchangeServiceEnvironment("ULTIMATE FALLBACK: 1:1 rate due to all providers failing")
                    .currencyConversionServiceEnvironment(
                            getServiceEnvironmentInfo() + " [ULTIMATE-FALLBACK]")
                    .build();

            return result;
        }
    }

    /**
     * Get service environment information including hostname and port
     * Essential for Kubernetes pod identification and debugging
     */
    private String getServiceEnvironmentInfo() {
        return CommonUtils.getServiceEnvironmentInfo("currency-conversion-service", serverPort);
    }
}