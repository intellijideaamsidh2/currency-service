package com.amsidh.mvc.service;

import com.amsidh.mvc.dto.CurrencyConversionDto;

import java.math.BigDecimal;

public interface CurrencyConversionService {
    CurrencyConversionDto convertCurrency(String fromCurrency, String toCurrency,
            BigDecimal quantity);
}