package com.amsidh.mvc.service.impl;

import com.amsidh.mvc.common.util.CommonUtils;
import com.amsidh.mvc.dto.CurrencyExchangeDto;
import com.amsidh.mvc.entity.CurrencyExchange;
import com.amsidh.mvc.repository.CurrencyExchangeRepository;
import com.amsidh.mvc.service.CurrencyExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {

    private final CurrencyExchangeRepository currencyExchangeRepository;

    @Value("${server.port}")
    private String port;

    @Override
    public CurrencyExchangeDto getCurrencyExchange(String fromCurrency, String toCurrency) {
        CurrencyExchange currencyExchange = currencyExchangeRepository
                .findByFromCurrencyAndToCurrency(fromCurrency.toUpperCase(), toCurrency.toUpperCase())
                .orElseThrow(() -> new RuntimeException(
                        "Currency exchange not found for " + fromCurrency + " to " + toCurrency));

        return CurrencyExchangeDto.builder()
                .fromCurrency(currencyExchange.getFromCurrency())
                .toCurrency(currencyExchange.getToCurrency())
                .rate(currencyExchange.getRate())
                .environment(getServiceEnvironmentInfo())
                .build();
    }

    /**
     * Get service environment information including hostname and port
     * Essential for Kubernetes pod identification and debugging
     */
    private String getServiceEnvironmentInfo() {
        return CommonUtils.getServiceEnvironmentInfo("currency-exchange-service", port);
    }
}