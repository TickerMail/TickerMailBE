package com.lambda.stocksubscription.stock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
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
public class StockApiService {

    @Value("${stock.api.key}")
    private String appKey;

    @Value("${stock.api.secret}")
    private String appSecret;

    @Value("${stock.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final TokenManager tokenManager;

    // 미국주식 현재가 조회
    public Map<String, Object> getUsStockPrice(String symbol, String excd) {
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

        log.info(response.toString());
        // 응답 처리
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to get US stock price for symbol: " + symbol);
        }
    }

    public void test() throws IOException {
        AsyncHttpClient client = new DefaultAsyncHttpClient();
        client.prepare("GET", "https://yahoo-finance15.p.rapidapi.com/api/v1/markets/quote?ticker=AAPL&type=STOCKS")
            .setHeader("x-rapidapi-key", "3cd7b89e3bmsh6da9223aa6ce497p17d456jsn4ed70a02fe73")
            .setHeader("x-rapidapi-host", "yahoo-finance15.p.rapidapi.com")
            .execute()
            .toCompletableFuture()
            .thenAccept(System.out::println)
            .join();

        client.close();
    }

}
