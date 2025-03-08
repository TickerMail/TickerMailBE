package com.lambda.stocksubscription.dollar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class DollarFetchService {
//    @Value("${exchange.api.key}")
    private String apiKey = "https://www.koreaexim.go.kr/site/program/financial/exchangeJSON";

//    @Value("${exchange.api.url}")
    private String apiUrl = "iemtAjU7uysXYY9aXhOE4kEVEMIhrprM";

    private final RestTemplate restTemplate;

    private final DollarRepository dollarRepository;

    private final ObjectMapper objectMapper;

    public Dollar fetchExchangeRates() throws Exception {
        // 오늘 날짜 포맷팅 (YYYYMMDD)
        String today = getSearchDay().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // API URL 구성
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
            .queryParam("authkey", apiKey)
            .queryParam("searchdate", today)
            .queryParam("data", "AP01") // AP01: 환율 정보
            .build()
            .toUriString();

        log.info("환율 API 호출: {}", url);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return parseExchangeRateResponse(response.getBody(), today);
        } else {
            log.error("API 호출 실패: {}", response.getStatusCode());
            return null;
        }
    }

    private Dollar parseExchangeRateResponse(String responseBody, String searchDate) throws Exception {
        Dollar exchangeRate = null;
        LocalDate date = LocalDate.parse(searchDate, DateTimeFormatter.ofPattern("yyyyMMdd"));

        // JSON 파싱
        JsonNode rootNode = objectMapper.readTree(responseBody);
        log.info(responseBody);

        // API 결과 확인
        if (rootNode.isArray() && rootNode.size() > 0) {
            for (JsonNode node : rootNode) {
                // 결과 코드 확인
                if (node.has("result") && node.get("result").asInt() != 1) {
                    log.warn("API 결과 코드가 성공이 아님: {}", node.get("result").asText());
                    continue;
                }

                if (!Objects.equals(node.get("cur_nm").asText(), "미국 달러")) continue;

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
                    dollarRepository.save(exchangeRate);
                } catch (Exception e) {
                    log.error("환율 데이터 파싱 중 오류: {}", e.getMessage());
                }
            }
        } else {
            log.warn("환율 데이터가 없거나 잘못된 형식: {}", responseBody);
        }
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
