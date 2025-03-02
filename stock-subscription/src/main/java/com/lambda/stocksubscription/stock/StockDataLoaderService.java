package com.lambda.stocksubscription.stock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockDataLoaderService implements CommandLineRunner {

    private final StockRepository stockRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // GitHub의 원본 데이터 URL
    private static final String NASDAQ_SYMBOLS_URL =
        "https://raw.githubusercontent.com/rreichel3/US-Stock-Symbols/main/nasdaq/nasdaq_full_tickers.json";
    private static final String NYSE_SYMBOLS_URL =
        "https://raw.githubusercontent.com/rreichel3/US-Stock-Symbols/main/nyse/nyse_full_tickers.json";

    @Override
    public void run(String... args) {
        try {
            // 애플리케이션 시작 시 데이터가 없으면 자동 로드
            if (stockRepository.count() == 0) {
                log.info("주식 심볼 데이터가 없습니다. GitHub에서 데이터를 로드합니다.");
                loadAllStockSymbols();
            } else {
                log.info("데이터베이스에 이미 {} 개의 주식 심볼이 있습니다.", stockRepository.count());
            }
        } catch (Exception e) {
            log.error("주식 심볼 데이터 로드 중 오류 발생", e);
        }
    }

    /**
     * 모든 주식 심볼 데이터 로드 (NASDAQ + NYSE)
     */
    @Transactional
    public void loadAllStockSymbols() {
        try {
            // NASDAQ 심볼 로드
            List<Stock> nasdaqStocks = loadStocksFromGitHub(NASDAQ_SYMBOLS_URL, "NAS");
            stockRepository.saveAll(nasdaqStocks);
            log.info("NASDAQ 주식 심볼 {} 개 로드 완료", nasdaqStocks.size());

            // NYSE 심볼 로드
            List<Stock> nyseStocks = loadStocksFromGitHub(NYSE_SYMBOLS_URL, "NYS");
            stockRepository.saveAll(nyseStocks);
            log.info("NYSE 주식 심볼 {} 개 로드 완료", nyseStocks.size());

            log.info("총 {} 개의 주식 심볼 데이터 로드 완료", nasdaqStocks.size() + nyseStocks.size());
        } catch (Exception e) {
            log.error("주식 심볼 데이터 로드 중 오류 발생", e);
            throw new RuntimeException("주식 심볼 데이터 로드 실패", e);
        }
    }

    /**
     * GitHub URL에서 주식 심볼 데이터 로드하여 Stock 엔티티로 변환
     */
    private List<Stock> loadStocksFromGitHub(String url, String exchange) {
        log.info("GitHub에서 {} 주식 심볼 데이터 다운로드: {}", exchange, url);

        // GitHub에서 JSON 데이터 가져오기
        String jsonResponse = restTemplate.getForObject(url, String.class);
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            log.error("GitHub에서 데이터를 가져오지 못했습니다");
            throw new RuntimeException("GitHub에서 데이터를 가져올 수 없습니다");
        }

        try {
            // JSON 배열 파싱
            List<Map<String, Object>> symbolsData = objectMapper.readValue(
                jsonResponse, new TypeReference<>() {});

            log.info("{} 개의 주식 심볼 데이터 파싱 완료", symbolsData.size());

            List<Stock> stocks = new ArrayList<>();

            for (Map<String, Object> data : symbolsData) {
                String symbol = (String) data.get("symbol");
                if (symbol == null || symbol.isEmpty()) {
                    continue; // 심볼이 없는 항목은 건너뜀
                }
                String name = (String) data.get("name");
                // 회사명에서 따옴표 등 정리
                if (name != null) {
                    name = name.replace("\"", "").trim();
                } else {
                    name = "";
                }
                Stock stock = Stock.builder()
                    .symbol(symbol)
                    .companyName(name)
                    .exchange(exchange)
                    .build();
                stocks.add(stock);
            }

            log.info("{} 주식 심볼 {}개 변환 완료", exchange, stocks.size());
            return stocks;
        } catch (Exception e) {
            log.error("JSON 파싱 중 오류 발생", e);
            throw new RuntimeException("JSON 파싱 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 기존 데이터 삭제 후 새로 로드 (수동 갱신 용)
     */
    @Transactional
    public void reloadAllStockSymbols() {
        log.info("기존 주식 심볼 데이터 삭제 및 재로드 시작");
        stockRepository.deleteAll();
        loadAllStockSymbols();
    }

    /**
     * 특정 거래소의 데이터만 새로 로드
     */
    @Transactional
    public void reloadExchangeSymbols(String exchange) {
        if ("NASDAQ".equalsIgnoreCase(exchange)) {
            log.info("NASDAQ 주식 심볼 재로드 시작");
            stockRepository.deleteByExchange("NASDAQ");
            List<Stock> nasdaqStocks = loadStocksFromGitHub(NASDAQ_SYMBOLS_URL, "NASDAQ");
            stockRepository.saveAll(nasdaqStocks);
            log.info("NASDAQ 주식 심볼 {} 개 재로드 완료", nasdaqStocks.size());
        } else if ("NYSE".equalsIgnoreCase(exchange)) {
            log.info("NYSE 주식 심볼 재로드 시작");
            stockRepository.deleteByExchange("NYSE");
            List<Stock> nyseStocks = loadStocksFromGitHub(NYSE_SYMBOLS_URL, "NYSE");
            stockRepository.saveAll(nyseStocks);
            log.info("NYSE 주식 심볼 {} 개 재로드 완료", nyseStocks.size());
        } else {
            throw new IllegalArgumentException("지원하지 않는 거래소입니다: " + exchange);
        }
    }
}