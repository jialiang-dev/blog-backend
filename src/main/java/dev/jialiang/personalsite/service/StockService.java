package dev.jialiang.personalsite.service;

import dev.jialiang.personalsite.entity.StockHolding;
import dev.jialiang.personalsite.entity.StockWatchlist;
import dev.jialiang.personalsite.repository.StockHoldingRepository;
import dev.jialiang.personalsite.repository.StockWatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockHoldingRepository holdingRepository;
    private final StockWatchlistRepository watchlistRepository;

    // ========== 持仓管理 ==========

    public List<StockHolding> listHoldings() {
        return holdingRepository.findAll();
    }

    public StockHolding addHolding(StockHolding holding) {
        return holdingRepository.findByStockCode(holding.getStockCode())
                .map(existing -> {
                    existing.setHoldShares(holding.getHoldShares());
                    existing.setCostPrice(holding.getCostPrice());
                    existing.setStockName(holding.getStockName());
                    return holdingRepository.save(existing);
                })
                .orElseGet(() -> holdingRepository.save(holding));
    }

    public void deleteHolding(Long id) {
        if (!holdingRepository.existsById(id)) {
            throw new RuntimeException("持仓记录不存在: " + id);
        }
        holdingRepository.deleteById(id);
    }

    // ========== 自选管理 ==========

    public List<StockWatchlist> listWatchlist() {
        return watchlistRepository.findAll();
    }

    public StockWatchlist addToWatchlist(StockWatchlist item) {
        return watchlistRepository.findByStockCode(item.getStockCode())
                .orElseGet(() -> watchlistRepository.save(item));
    }

    @Transactional
    public void removeFromWatchlist(String stockCode) {
        watchlistRepository.deleteByStockCode(stockCode);
    }

    public void removeFromWatchlistById(Long id) {
        if (!watchlistRepository.existsById(id)) {
            throw new RuntimeException("自选记录不存在: " + id);
        }
        watchlistRepository.deleteById(id);
    }

    /**
     * 获取所有关注的股票代码（持仓 + 自选，去重）
     */
    public List<String> getAllTrackedStockCodes() {
        List<String> holdingCodes = holdingRepository.findAll().stream()
                .map(StockHolding::getStockCode)
                .toList();
        List<String> watchlistCodes = watchlistRepository.findAll().stream()
                .map(StockWatchlist::getStockCode)
                .toList();
        return java.util.stream.Stream.concat(holdingCodes.stream(), watchlistCodes.stream())
                .distinct()
                .toList();
    }
}
