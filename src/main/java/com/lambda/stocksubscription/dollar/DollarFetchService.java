package com.lambda.stocksubscription.dollar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.stocksubscription.util.CircuitBreaker;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
        LocalDate today = LocalDate.now();

        DayOfWeek dayOfWeek = today.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.MONDAY) {
            return today.minusDays(3);
        } else if (dayOfWeek == DayOfWeek.SUNDAY) {
            return today.minusDays(2);
        } else if (dayOfWeek == DayOfWeek.SATURDAY) {
            return today.minusDays(1);
        }
        return today;
    }
}
