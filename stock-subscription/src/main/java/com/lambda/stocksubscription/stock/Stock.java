package com.lambda.stocksubscription.stock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name="stock")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Stock {
    @Id
    private String symbol;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String exchange;
}
