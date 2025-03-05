package com.lambda.stocksubscription.stock;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StockDTO {
    private String ticker;
    private String name;
    private String exchange;
}
