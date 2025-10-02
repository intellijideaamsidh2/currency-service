package com.amsidh.mvc.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amsidh.mvc.dto.CurrencyExchangeDto;
import com.amsidh.mvc.service.CurrencyExchangeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Currency Exchange", description = "Endpoints to fetch exchange rates")
@RequestMapping("/currencyexchange")
@RequiredArgsConstructor
public class CurrencyExchangeController {

    private final CurrencyExchangeService currencyExchangeService;

    @GetMapping(value = "/{fromCurrency}/to/{toCurrency}/rate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get exchange rate", description = "Gets the current exchange rate for a currency pair")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rate found",
            content = @Content(schema = @Schema(implementation = CurrencyExchangeDto.class))),
        @ApiResponse(responseCode = "404", description = "Rate not found", content = @Content)
    })
    public CurrencyExchangeDto getCurrencyExchange(
            @Parameter(description = "Source currency code", example = "USD") @PathVariable String fromCurrency,
            @Parameter(description = "Target currency code", example = "INR") @PathVariable String toCurrency) {
        return currencyExchangeService.getCurrencyExchange(fromCurrency, toCurrency);
    }
}