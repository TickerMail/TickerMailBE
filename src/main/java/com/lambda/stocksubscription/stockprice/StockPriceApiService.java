package com.lambda.stocksubscription.stockprice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import lombok.RequiredArgsConstructor;import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockPriceApiService {

    @Value("${stock.api.key}")
    private String appKey;

    @Value("${stock.api.secret}")
    private String appSecret;

    @Value("${stock.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final TokenManager tokenManager;

    // 미국주식 현재가 조회
    public StockPrice getUsStockPrices(String symbol, String excd) {
        tokenManager.validateToken();

        String url = baseUrl + "/price" + "?AUTH=&EXCD=" + excd + "&SYMB=" + symbol;

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(tokenManager.getAccessToken());
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "HHDFS00000300"); // 미국주식 현재가 조회 TR ID

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(headers);

        // API 호출
        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            Map.class
        );

        log.info(response.getBody().get("output").toString());
        Map output = (Map) response.getBody().get("output");

        StockPrice stockPrice = StockPrice
            .builder()
            .symbol(symbol)
            .closingPrice(new BigDecimal(String.valueOf(output.get("last"))))
            .changeAmount(new BigDecimal(String.valueOf(output.get("diff"))))
            .changePercent(new BigDecimal(String.valueOf(output.get("rate"))))
            .tradingDate(LocalDate.now(ZoneId.of("Asia/Seoul")))
            .build();

        // 응답 처리
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return stockPrice;
        } else {
            throw new RuntimeException("Failed to get US stock price for symbol: " + symbol);
        }
    }
}
