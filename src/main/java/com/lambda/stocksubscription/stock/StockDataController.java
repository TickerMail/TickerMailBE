package com.lambda.stocksubscription.stock;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockDataController {
    private final StockDataLoaderService stockDataLoaderService;

    @GetMapping("/all")
    public ResponseEntity<List<StockDTO>> getAllStocks() {
        List<StockDTO> allStocks = stockDataLoaderService.loadAllStocks();
        return ResponseEntity.ok(allStocks);
    }
}
