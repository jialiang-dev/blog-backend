package com.yourdomain.personalsite.repository;

import com.yourdomain.personalsite.entity.StockWatchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockWatchlistRepository extends JpaRepository<StockWatchlist, Long> {
    Optional<StockWatchlist> findByStockCode(String stockCode);
    void deleteByStockCode(String stockCode);
}
