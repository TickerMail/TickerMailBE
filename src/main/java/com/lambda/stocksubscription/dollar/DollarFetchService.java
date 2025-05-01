package com.lambda.stocksubscription.dollar;

import com.lambda.stocksubscription.util.CircuitBreaker;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DollarFetchService {
    @Value("${exchange.api.key}")
    private String apiKey;

    @Value("${exchange.api.url}")
    private String apiUrl;

    private final DollarRepository dollarRepository;
    private final CircuitBreaker circuitBreaker;

    @Scheduled(cron = "0 0 8 * * ?", zone = "Asia/Seoul")
    public Dollar fetchExchangeRates() throws Exception {
        // 오늘 날짜 포맷팅 (YYYYMMDD)
        String today = getSearchDay().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // API URL 구성
        String url = apiUrl;

        log.info("환율 API 호출: {}", url);

        Dollar exchangeRate = circuitBreaker.fetchDollar(url, today);
        log.info(exchangeRate.toString());
        dollarRepository.save(exchangeRate);
        return exchangeRate;
    }

    private LocalDate getSearchDay() {
        return LocalDate.now(ZoneId.of("Asia/Seoul"));
    }
}
