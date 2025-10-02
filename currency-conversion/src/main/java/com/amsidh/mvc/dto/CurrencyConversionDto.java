package com.amsidh.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyConversionDto {
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal quantity;
    private BigDecimal rate;
    private BigDecimal totalAmount;
    private String currencyExchangeServiceEnvironment;
    private String currencyConversionServiceEnvironment;
}