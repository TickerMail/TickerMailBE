package com.lambda.stocksubscription.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.stocksubscription.dollar.Dollar;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Parser {

    private final ObjectMapper objectMapper;

    public Dollar parseExchangeRateResponse(String responseBody, LocalDate date) throws Exception {
        // JSON 파싱
        JsonNode rootNode = objectMapper.readTree(responseBody);
        log.info("responsebody " + responseBody);

        // API 결과 확인
        JsonNode node = rootNode.get("country");

        try {
            // 숫자 데이터 파싱 (콤마 제거)
            String dollarValue = node.get(1).get("value").asText();

            Dollar rate = Dollar.builder()
                .dollarValue(dollarValue)
                .searchDate(date)
                .build();

            log.info(rate.toString());
            return rate;
        } catch (Exception e) {
            log.error("환율 데이터 파싱 중 오류: {}", e.getMessage());
            throw new Exception("환율 데이터 파싱 중 오류");
        }
    }
}
