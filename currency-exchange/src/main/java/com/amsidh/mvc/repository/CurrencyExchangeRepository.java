package com.amsidh.mvc.repository;

import com.amsidh.mvc.entity.CurrencyExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyExchangeRepository extends JpaRepository<CurrencyExchange, Integer> {

    @Query("SELECT ce FROM CurrencyExchange ce WHERE ce.fromCurrency = :fromCurrency AND ce.toCurrency = :toCurrency")
    Optional<CurrencyExchange> findByFromCurrencyAndToCurrency(@Param("fromCurrency") String fromCurrency,
            @Param("toCurrency") String toCurrency);
}