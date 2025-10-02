package com.amsidh.mvc.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.amsidh.mvc.dto.CurrencyExchangeDto;

@FeignClient(
    name = "currency-exchange",
    url = "${CURRENCY_EXCHANGE_SERVICE_URL:http://localhost:8181}"
)
public interface CurrencyExchangeClient {

    @GetMapping("/currencyexchange/{fromCurrency}/to/{toCurrency}/rate")
    CurrencyExchangeDto getExchangeRate(
            @PathVariable String fromCurrency,
            @PathVariable String toCurrency);
}