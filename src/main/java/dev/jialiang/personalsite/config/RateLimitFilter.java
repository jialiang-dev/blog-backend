package dev.jialiang.personalsite.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简易 IP 限流：每分钟最多 N 次请求
 */
@Slf4j
public class RateLimitFilter implements Filter {

    // 读操作：每分钟 120 次
    private static final int READ_LIMIT = 120;
    // 写操作：每分钟 30 次
    private static final int WRITE_LIMIT = 30;

    private final Map<String, AtomicInteger> readCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> writeCounters = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String ip = httpReq.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = httpReq.getRemoteAddr();
        }
        int minuteKey = (int) (System.currentTimeMillis() / 60000);

        String method = httpReq.getMethod();
        boolean isWrite = "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);

        String counterKey = ip + ":" + minuteKey;
        Map<String, AtomicInteger> counters = isWrite ? writeCounters : readCounters;
        int limit = isWrite ? WRITE_LIMIT : READ_LIMIT;

        AtomicInteger counter = counters.computeIfAbsent(counterKey, k -> new AtomicInteger(0));

        // 清理旧计数器（超过 2 分钟的）
        if (counterKey.contains(String.valueOf(minuteKey - 2))) {
            counters.entrySet().removeIf(e -> e.getKey().endsWith(String.valueOf(minuteKey - 2)));
        }

        if (counter.incrementAndGet() > limit) {
            httpResp.setStatus(429);
            httpResp.setContentType("application/json;charset=UTF-8");
            httpResp.getWriter().write("{\"code\":429,\"data\":null,\"message\":\"请求过于频繁，请稍后再试\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}
