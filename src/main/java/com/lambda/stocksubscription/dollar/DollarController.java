package com.lambda.stocksubscription.dollar;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dollar")
@RequiredArgsConstructor
public class DollarController {

    private final DollarFetchService dollarFetchService;

    @GetMapping("/test")
    public ResponseEntity<Dollar> fetchDollar() throws Exception {
        return ResponseEntity.ok().body(dollarFetchService.fetchExchangeRates());
    }
}
