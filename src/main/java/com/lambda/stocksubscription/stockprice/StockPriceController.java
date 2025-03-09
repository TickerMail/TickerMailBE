package com.lambda.stocksubscription.stockprice;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock/price")
@RequiredArgsConstructor
public class StockPriceController {
    private final StockPriceApiService stockApiService;
    private final StockPriceTraceService stockPriceTraceService;

    @GetMapping("/update")
    public void updateAllInterestStock() {
        stockPriceTraceService.updateDailyStockPrices();
    }
}
