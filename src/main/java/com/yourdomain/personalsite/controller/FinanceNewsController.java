package com.yourdomain.personalsite.controller;

import com.yourdomain.personalsite.dto.ApiResponse;
import com.yourdomain.personalsite.entity.FinanceNews;
import com.yourdomain.personalsite.service.FinanceNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceNewsController {

    private final FinanceNewsService newsService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String NEWS_CACHE_KEY = "finance:news:latest";
    private static final long CACHE_TTL_MINUTES = 30;

    @GetMapping("/news")
    @SuppressWarnings("unchecked")
    public ApiResponse<List<FinanceNews>> getLatestNews() {
        // 先查 Redis 缓存
        Object cached = redisTemplate.opsForValue().get(NEWS_CACHE_KEY);
        if (cached instanceof List) {
            return ApiResponse.success((List<FinanceNews>) cached, "from cache");
        }

        List<FinanceNews> news = newsService.getLatestNews(50);
        // 写入缓存
        redisTemplate.opsForValue().set(NEWS_CACHE_KEY, news, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return ApiResponse.success(news);
    }
}
