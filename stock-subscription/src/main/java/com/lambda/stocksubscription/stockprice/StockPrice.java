package com.lambda.stocksubscription.stockprice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_price")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    // ticker
    @Column(nullable = false)
    private String symbol;

    // 전일 종가 미국 기준으로는 장 마감 후 당일
    @Column(nullable = false)
    private BigDecimal closingPrice;

    // 전일 미국 시간으로는 장 마감 날짜
    @Column(nullable = false)
    private LocalDate tradingDate;
}
