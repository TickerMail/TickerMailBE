package com.lambda.stocksubscription.dollar;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DollarController {

    private final DollarFetchService dollarFetchService;

    @GetMapping("/test/dollar")
    public void fetchDollar() throws Exception {
        dollarFetchService.fetchExchangeRates();
    }
}
