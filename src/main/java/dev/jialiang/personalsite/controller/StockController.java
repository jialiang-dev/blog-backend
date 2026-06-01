package dev.jialiang.personalsite.controller;

import dev.jialiang.personalsite.dto.ApiResponse;
import dev.jialiang.personalsite.entity.StockHolding;
import dev.jialiang.personalsite.entity.StockWatchlist;
import dev.jialiang.personalsite.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    // ========== 持仓 ==========

    @GetMapping("/holdings")
    public ApiResponse<List<StockHolding>> listHoldings() {
        return ApiResponse.success(stockService.listHoldings());
    }

    @PostMapping("/holdings")
    public ApiResponse<StockHolding> addHolding(@RequestBody StockHolding holding) {
        return ApiResponse.success(stockService.addHolding(holding));
    }

    @DeleteMapping("/holdings/{id}")
    public ApiResponse<Void> deleteHolding(@PathVariable Long id) {
        stockService.deleteHolding(id);
        return ApiResponse.success(null);
    }

    // ========== 自选 ==========

    @GetMapping("/watchlist")
    public ApiResponse<List<StockWatchlist>> listWatchlist() {
        return ApiResponse.success(stockService.listWatchlist());
    }

    @PostMapping("/watchlist")
    public ApiResponse<StockWatchlist> addToWatchlist(@RequestBody StockWatchlist item) {
        return ApiResponse.success(stockService.addToWatchlist(item));
    }

    @DeleteMapping("/watchlist/{id}")
    public ApiResponse<Void> removeFromWatchlist(@PathVariable Long id) {
        stockService.removeFromWatchlistById(id);
        return ApiResponse.success(null);
    }
}
