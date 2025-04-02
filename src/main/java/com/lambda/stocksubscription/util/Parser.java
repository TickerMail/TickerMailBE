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
        Dollar exchangeRate = null;

        // JSON 파싱
        JsonNode rootNode = objectMapper.readTree(responseBody);
        log.info(responseBody);

        // API 결과 확인
        if (rootNode.isArray() && rootNode.size() > 0) {
            for (JsonNode node : rootNode) {
                // 결과 코드 확인
                if (node.has("result") && node.get("result").asInt() != 1) {
                    log.warn("API 결과 코드가 성공이 아님: {}", node.get("result").asText());
                    throw new Exception("환율 API 결과 코드가 성공이 아님");
                }

                if (!Objects.equals(node.get("cur_nm").asText(), "미국 달러")) throw new Exception("달러가 없음");

                try {
                    // 숫자 데이터 파싱 (콤마 제거)
                    String ttBuyingRateStr = node.has("ttb") ? node.get("ttb").asText().replace(",", "") : "0";
                    String ttSellingRateStr = node.has("tts") ? node.get("tts").asText().replace(",", "") : "0";

                    BigDecimal ttBuyingRate = new BigDecimal(ttBuyingRateStr);
                    BigDecimal ttSellingRate = new BigDecimal(ttSellingRateStr);

                    Dollar rate = Dollar.builder()
                        .buyingRate(ttBuyingRate)
                        .sellingRate(ttSellingRate)
                        .searchDate(date)
                        .build();

                    exchangeRate = rate;
                    log.info(rate.toString());
                } catch (Exception e) {
                    log.error("환율 데이터 파싱 중 오류: {}", e.getMessage());
                    throw new Exception("환율 데이터 파싱 중 오류");
                }
            }
        } else {
            log.warn("환율 데이터가 없거나 잘못된 형식: {}", responseBody);
            throw new Exception("환율 데이터가 없거나 잘못된 형식");
        }
        return exchangeRate;
    }
}
