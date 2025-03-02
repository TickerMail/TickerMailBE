package com.lambda.stocksubscription.stock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findBySymbolAndTradingDate(String symbol, LocalDate tradingDate);

    List<Stock> findByTradingDate(LocalDate tradingDate);

    @Query("SELECT DISTINCT s.symbol FROM Stock s")
    List<String> findAllDistinctSymbols();

    List<Stock> findBySymbolInAndTradingDate(List<String> symbols, LocalDate tradingDate);

    String findCompanyNameBySymbol(String symbol);
}
