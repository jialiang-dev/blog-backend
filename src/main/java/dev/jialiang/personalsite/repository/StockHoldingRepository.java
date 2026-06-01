package dev.jialiang.personalsite.repository;

import dev.jialiang.personalsite.entity.StockHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {
    Optional<StockHolding> findByStockCode(String stockCode);
}
