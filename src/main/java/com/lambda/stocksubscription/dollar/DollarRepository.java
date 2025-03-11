package com.lambda.stocksubscription.dollar;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DollarRepository extends JpaRepository<Dollar, Long> {
    Dollar findBySearchDate(LocalDate searchDate);
}
