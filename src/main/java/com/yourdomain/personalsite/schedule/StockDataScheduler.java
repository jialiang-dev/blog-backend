package com.yourdomain.personalsite.schedule;

import com.yourdomain.personalsite.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockDataScheduler {

    private final StockService stockService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Random random = new Random();

    private static final Map<String, BigDecimal> BASE_PRICES = new HashMap<>();

    static {
        // 模拟基础价格
        BASE_PRICES.put("000001", new BigDecimal("12.50"));  // 平安银行
        BASE_PRICES.put("600519", new BigDecimal("1680.00")); // 贵州茅台
        BASE_PRICES.put("000858", new BigDecimal("158.00"));  // 五粮液
        BASE_PRICES.put("300750", new BigDecimal("210.00"));  // 宁德时代
        BASE_PRICES.put("002415", new BigDecimal("35.60"));   // 海康威视
        BASE_PRICES.put("600036", new BigDecimal("38.20"));   // 招商银行
        BASE_PRICES.put("000333", new BigDecimal("65.80"));   // 美的集团
        BASE_PRICES.put("601318", new BigDecimal("46.50"));   // 中国平安
    }

    @Scheduled(fixedRate = 10000)
    public void syncStockData() {
        List<String> stockCodes = stockService.getAllTrackedStockCodes();
        if (stockCodes.isEmpty()) {
            return;
        }

        for (String code : stockCodes) {
            try {
                Map<String, Object> data = generateStockData(code);
                redisTemplate.opsForValue().set("stock:realtime:" + code, data, 30, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("生成股票数据失败 [{}]: {}", code, e.getMessage());
            }
        }
    }

    private Map<String, Object> generateStockData(String code) {
        BigDecimal basePrice = BASE_PRICES.getOrDefault(code, new BigDecimal("10.00"));

        // 模拟 ±3% 的随机波动
        double changePercent = (random.nextDouble() * 6 - 3);
        BigDecimal change = basePrice.multiply(BigDecimal.valueOf(changePercent / 100))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal currentPrice = basePrice.add(change);

        Map<String, Object> data = new HashMap<>();
        data.put("stockCode", code);
        data.put("stockName", getStockName(code));
        data.put("currentPrice", currentPrice);
        data.put("change", change);
        data.put("changePercent", BigDecimal.valueOf(changePercent).setScale(2, RoundingMode.HALF_UP));
        return data;
    }

    private String getStockName(String code) {
        return switch (code) {
            case "000001" -> "平安银行";
            case "600519" -> "贵州茅台";
            case "000858" -> "五粮液";
            case "300750" -> "宁德时代";
            case "002415" -> "海康威视";
            case "600036" -> "招商银行";
            case "000333" -> "美的集团";
            case "601318" -> "中国平安";
            default -> code;
        };
    }
}
