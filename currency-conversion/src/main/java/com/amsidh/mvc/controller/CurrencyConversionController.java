package com.amsidh.mvc.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amsidh.mvc.dto.CurrencyConversionDto;
import com.amsidh.mvc.service.CurrencyConversionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Tag(name = "Currency Conversion", description = "Endpoints to convert currency amounts using exchange rates")
@RequestMapping("/currencyconversion")
@RequiredArgsConstructor
public class CurrencyConversionController {

    private final CurrencyConversionService currencyConversionService;

    @GetMapping("/{fromCurrency}/to/{toCurrency}/{quantity}/calculate")
    @Operation(
        summary = "Convert currency amount",
        description = "Converts a specified quantity from one currency to another using the current exchange rate"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful conversion",
            content = @Content(schema = @Schema(implementation = CurrencyConversionDto.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content)
    })
    public ResponseEntity<CurrencyConversionDto> convertCurrency(
            @Parameter(description = "Source currency code", example = "USD") @PathVariable String fromCurrency,
            @Parameter(description = "Target currency code", example = "INR") @PathVariable String toCurrency,
            @Parameter(description = "Amount to convert", example = "100.00") @PathVariable BigDecimal quantity) {

        log.debug("Received currency conversion request: {} to {} with quantity {}",
                fromCurrency, toCurrency, quantity);

        try {
            CurrencyConversionDto result = currencyConversionService.convertCurrency(fromCurrency,
                    toCurrency, quantity);
            log.debug("Currency conversion completed successfully");
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Currency conversion failed: {}", ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}