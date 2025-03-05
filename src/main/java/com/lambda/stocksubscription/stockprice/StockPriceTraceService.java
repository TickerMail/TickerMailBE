package com.lambda.stocksubscription.stockprice;

import com.lambda.stocksubscription.stock.StockRepository;
import com.lambda.stocksubscription.user.UserService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockPriceTraceService {

    private final StockPriceApiService stockApiService;
    private final StockPriceRepository stockPriceRepository;
    private final StockRepository stockRepository;
    private final UserService userService;

    @Scheduled(cron = "0 0 7 * * ?") // 매일 오전 7시에 실행
    @Transactional
    public void updateDailyStockPrices() {
        log.info("Starting daily stock price update...");

        // 1. 현재 시스템에 등록된 모든 관심 종목 목록 가져오기
        Set<String> allInterestedSymbols = userService.getAllInterestedStocks();
        List<String> symbols = new ArrayList<>(allInterestedSymbols);

        // 3. API에서 주식 데이터 가져오기
        List<StockPrice> stockPrices = new ArrayList<>();
        for (String symbol : symbols) {
            String exchange = stockRepository.findBySymbol(symbol).getExchange();
            stockPrices.add(stockApiService.getUsStockPrices(symbol, exchange));
        }

        // 4. 데이터베이스에 저장
        stockPriceRepository.saveAll(stockPrices);

        log.info("Completed daily stock price update for {} symbols", stockPrices.size());
    }
}
