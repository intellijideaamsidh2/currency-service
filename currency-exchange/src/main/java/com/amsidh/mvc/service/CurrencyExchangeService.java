package com.amsidh.mvc.service;

import com.amsidh.mvc.dto.CurrencyExchangeDto;

public interface CurrencyExchangeService {
    CurrencyExchangeDto getCurrencyExchange(String fromCurrency, String toCurrency);
}