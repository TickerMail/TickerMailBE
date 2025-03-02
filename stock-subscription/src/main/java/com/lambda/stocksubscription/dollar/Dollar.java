package com.lambda.stocksubscription.dollar;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Data
public class Dollar {

    @Id
    @Column(nullable = false)
    private LocalDate searchDate;

    private BigDecimal buyingRate;

    private BigDecimal sellingRate;
}
