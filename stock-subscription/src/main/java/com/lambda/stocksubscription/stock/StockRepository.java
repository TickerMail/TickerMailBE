package com.lambda.stocksubscription.stock;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, String> {

    /**
     * 특정 거래소의 모든 주식 조회
     */
    List<Stock> findByExchange(String exchange);

    /**
     * 회사명에 특정 키워드가 포함된 주식 조회
     */
    List<Stock> findByCompanyNameContainingIgnoreCase(String keyword);

    /**
     * 심볼에 특정 키워드가 포함된 주식 조회
     */
    List<Stock> findBySymbolContainingIgnoreCase(String keyword);

    /**
     * 특정 거래소의 모든 주식 삭제 (관리자 기능)
     */
    @Modifying
    @Query("DELETE FROM Stock s WHERE s.exchange = ?1")
    void deleteByExchange(String exchange);

    /**
     * 심볼 목록으로 주식 목록 조회
     */
    List<Stock> findBySymbolIn(List<String> symbols);

    /**
     * 특정 거래소의 주식 수 카운트
     */
    long countByExchange(String exchange);
}