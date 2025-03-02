package com.lambda.stocksubscription.dollar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DollarRepository extends JpaRepository<Dollar, Long> {

}
