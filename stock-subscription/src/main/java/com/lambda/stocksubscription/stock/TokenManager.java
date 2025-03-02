package com.lambda.stocksubscription.stock;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class TokenManager {
    @Value("${stock.api.key}")
    private String appKey;

    @Value("${stock.api.secret}")
    private String appSecret;

    private final RestTemplate restTemplate;

    @Getter
    private String accessToken;

    private static final long EXPIRES_IN = 24 * 60 * 60;

    private static final long THRESHHOLD = 60 * 60 * 1000;

    private long tokenExpiryTime;

    // 토큰 발급 메서드
    public void issueAccessToken() {
        String tokenUrl = "https://openapi.koreainvestment.com:9443/oauth2/tokenP";

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 바디 설정
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", appKey);
        body.put("appsecret", appSecret);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        // API 호출
        ResponseEntity<Map> response = restTemplate.exchange(
            tokenUrl,
            HttpMethod.POST,
            entity,
            Map.class
        );

        // 응답에서 토큰 추출
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            accessToken = (String) response.getBody().get("access_token");
            tokenExpiryTime = System.currentTimeMillis() + (EXPIRES_IN * 1000L);
        } else {
            throw new RuntimeException("Failed to get access token");
        }
    }

    // 토큰 유효성 검사 및 필요시 갱신
    public void validateToken() {
        if (accessToken == null || System.currentTimeMillis() >= tokenExpiryTime - THRESHHOLD) {
            issueAccessToken();
        }
    }
}
