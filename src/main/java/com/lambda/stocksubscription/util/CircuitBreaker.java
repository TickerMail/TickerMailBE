package com.lambda.stocksubscription.util;

import com.lambda.stocksubscription.dollar.Dollar;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CircuitBreaker {

    private static final int MAX_RETRIES = 10;

    private enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    private final RestTemplate restTemplate;
    private final Parser parser;
    private static final int RESET_TIMEOUT_SECONDS = 10;      // 10초 후 요청이 오면 반열림 상태로 전환
    private State state = State.CLOSED;
    private LocalDateTime openStateTime;

    public Dollar fetchDollar(String url, String searchDate) {
        LocalDate date = LocalDate.parse(searchDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
        switch (state) {
            case CLOSED:
                return callService(url, date);
            case OPEN:
                try {
                    if (openStateTime.plusSeconds(RESET_TIMEOUT_SECONDS).isBefore(LocalDateTime.now())) {
                        state = State.HALF_OPEN;
                        return callServiceHalfOpen(url, date);
                    } else {
                        return getDefaultDollar(date);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Circuit Breaker is OPEN");
                }
            case HALF_OPEN:
                return callServiceHalfOpen(url, date);
            default:
                throw new RuntimeException("Invalid state");
        }
    }

    private Dollar callService(String url, LocalDate date) {
        log.info("API 호출: {}", url);
        // 최대 재시도 횟수만큼 시도
        for (int attempt = 0; attempt < RESET_TIMEOUT_SECONDS; attempt++) {
            try {
                if (attempt > 0) {
                    // 재시도 전 잠시 대기
                    Thread.sleep(60000);
                    log.info("API 호출 재시도 {}/{}", attempt + 1, MAX_RETRIES);
                }

                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                return parser.parseExchangeRateResponse(response.getBody(), date);
            } catch (Exception e) {
                log.warn("API 호출 실패 (시도 {}/{}): {}", attempt + 1, MAX_RETRIES, e.getMessage());
            }
        }

        // 실패 임계값 초과 시 서킷 열기
        if (state == State.CLOSED) {
                log.info("서킷 브레이커 상태 변경: CLOSED -> OPEN (연속 실패: {})", MAX_RETRIES);
                state = State.OPEN;
                openStateTime = LocalDateTime.now();
        }

        log.info("서킷 브레이커 상태: {}", state);
        return getDefaultDollar(date);
    }

    private Dollar callServiceHalfOpen(String url, LocalDate date) {
        try {
            // 재시도 없이 한 번만 시도
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            Dollar result = parser.parseExchangeRateResponse(response.getBody(), date);

            // 성공 시 CLOSED 상태로 전환
            if (state == State.HALF_OPEN) {
                log.info("서킷 브레이커 상태 변경: HALF_OPEN -> CLOSED");
                state = State.CLOSED;
            }

            return result;
        } catch (Exception e) {
            // 실패 시 다시 OPEN 상태로 전환
            if (state == State.HALF_OPEN) {
                log.info("서킷 브레이커 상태 변경: HALF_OPEN -> OPEN");
                state = State.OPEN;
                openStateTime = LocalDateTime.now();
            }
            log.error("HALF_OPEN 상태에서 요청 실패: {}", e.getMessage());
            return getDefaultDollar(date);
        }
    }

    private Dollar getDefaultDollar(LocalDate date) {
        return Dollar.builder()
            .buyingRate(null)
            .sellingRate(null)
            .searchDate(date)
            .build();
    }
}
