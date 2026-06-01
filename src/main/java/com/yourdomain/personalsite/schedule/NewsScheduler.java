package com.yourdomain.personalsite.schedule;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourdomain.personalsite.entity.FinanceNews;
import com.yourdomain.personalsite.service.FinanceNewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsScheduler {

    private final FinanceNewsService newsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 华尔街见闻 7×24 快讯（简短硬核中文内容）
    private static final String WALLSTREETCN_LIVES =
            "https://api-one.wallstcn.com/apiv1/content/lives?channel=global-channel&limit=20";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");

    @Scheduled(initialDelay = 5000, fixedRate = 1800000) // 启动 5 秒后首次执行，之后每 30 分钟
    public void fetchNews() {
        log.info("开始抓取财经资讯...");
        int saved = 0;
        try {
            saved = fetchWallstreetcn();
        } catch (Exception e) {
            log.error("抓取华尔街见闻失败: {}", e.getMessage());
        }
        log.info("财经资讯抓取完成，本次新增 {} 条", saved);
    }

    @Scheduled(initialDelay = 15000, fixedRate = 3600000) // 启动 15 秒后首次执行，之后每小时
    public void cleanOldNews() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(48);
        int deleted = newsService.deleteOlderThan(cutoff);
        if (deleted > 0) {
            log.info("清理 {} 条 48 小时前的旧资讯", deleted);
        }
    }

    private int fetchWallstreetcn() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(WALLSTREETCN_LIVES))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)")
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            log.warn("华尔街见闻 API 返回: {}", response.statusCode());
            return 0;
        }

        JsonNode root = objectMapper.readTree(response.body());
        if (root.get("code").asInt() != 20000) {
            log.warn("华尔街见闻 API 错误: {}", root.get("message").asText(""));
            return 0;
        }

        JsonNode items = root.get("data").get("items");
        int count = 0;
        for (JsonNode item : items) {
            if (count >= 20) break;

            long id = item.get("id").asLong();
            String url = "https://wallstreetcn.com/livenews/" + id;

            if (newsService.existsByUrl(url)) continue; // 去重

            String title = item.has("title") ? item.get("title").asText() : "";
            if (title.isEmpty()) title = "快讯";

            String text = item.has("content_text") ? item.get("content_text").asText("") : "";
            text = HTML_TAG.matcher(text).replaceAll("").trim();
            if (text.length() > 500) text = text.substring(0, 500);

            long displayTime = item.get("display_time").asLong();
            LocalDateTime publishedAt = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(displayTime), ZoneId.of("Asia/Shanghai"));

            newsService.save(FinanceNews.builder()
                    .title(title)
                    .url(url)
                    .summary(text)
                    .source("华尔街见闻")
                    .publishedAt(publishedAt)
                    .build());
            count++;
        }
        return count;
    }
}
