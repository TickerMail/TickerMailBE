package com.lambda.stocksubscription.stockprice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
    Optional<StockPrice> findBySymbolAndTradingDate(String symbol, LocalDate tradingDate);

    List<StockPrice> findByTradingDate(LocalDate tradingDate);

    @Query("SELECT DISTINCT s.symbol FROM StockPrice s")
    List<String> findAllDistinctSymbols();

    List<StockPrice> findBySymbolInAndTradingDate(List<String> symbols, LocalDate tradingDate);

    String findCompanyNameBySymbol(String symbol);
}
