package dev.jialiang.personalsite.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockWebSocketHandler extends TextWebSocketHandler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, ScheduledFuture<?>> sessionTasks = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("WebSocket 连接建立: {}", session.getId());

        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
            try {
                if (session.isOpen()) {
                    pushStockData(session);
                }
            } catch (Exception e) {
                log.error("推送股票数据失败: {}", e.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS);

        sessionTasks.put(session.getId(), task);
    }

    private void pushStockData(WebSocketSession session) throws Exception {
        Set<String> keys = redisTemplate.keys("stock:realtime:*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        List<Object> dataList = redisTemplate.opsForValue().multiGet(keys);
        String json = objectMapper.writeValueAsString(dataList);
        session.sendMessage(new TextMessage(json));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        ScheduledFuture<?> task = sessionTasks.remove(session.getId());
        if (task != null) {
            task.cancel(false);
        }
        log.info("WebSocket 连接关闭: {}", session.getId());
    }
}
