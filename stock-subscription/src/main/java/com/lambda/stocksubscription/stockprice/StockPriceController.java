package com.lambda.stocksubscription.stockprice;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StockPriceController {
    private final StockPriceApiService stockApiService;
}
