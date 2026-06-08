package dev.jialiang.personalsite.controller;

import dev.jialiang.personalsite.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
public class IndexController {

    // 新浪财经实时行情 API（免费，无需 key）
    private static final String SINA_URL =
        "http://hq.sinajs.cn/list=s_sh000001,s_sz399006," +
        "gb_inx,gb_ndx,rt_hkHSI";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    // 缓存最近一次非零涨跌幅，避免休市时显示 0%
    private final Map<String, Double> lastChangeCache = new java.util.concurrent.ConcurrentHashMap<>();

    @GetMapping("/indices")
    public ApiResponse<List<Map<String, Object>>> getIndices() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(SINA_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("Referer", "https://finance.sina.com.cn")
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            List<Map<String, Object>> result = parseSinaResponse(resp.body());
            if (result.isEmpty()) {
                return ApiResponse.error(502, "新浪 API 返回空数据");
            }
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(502, "无法连接新浪 API: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> parseSinaResponse(String body) {
        List<Map<String, Object>> result = new ArrayList<>();
        String[] lines = body.split("\n");
        for (String line : lines) {
            if (!line.contains("=")) continue;
            // 前缀决定字段格式:
            //   rt_ = 港股实时, gb_ = 美股指数, s_/int_ = A股/国际指数
            boolean isHK = line.contains("hq_str_rt_");
            boolean isUS = line.contains("hq_str_gb_");
            String data = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
            String[] fields = data.split(",");
            if (fields.length < 4) continue;

            String name;
            double price;
            double changePct;

            if (isHK) {
                // 港股格式: 英文简称,中文名,当前价,昨收,最高,最低,开盘,涨跌额,涨跌幅%,...
                name = fields[1];
                price = parseDouble(fields[2]);
                changePct = parseDouble(fields[8]);
            } else if (isUS) {
                // 美股指数格式: 名称,当前价,涨跌幅%,时间戳,涨跌额,...
                name = fields[0];
                price = parseDouble(fields[1]);
                changePct = parseDouble(fields[2]);
            } else {
                // A股/国际指数格式: 名称,当前价,涨跌额,涨跌幅%,...
                name = fields[0];
                price = parseDouble(fields[1]);
                changePct = parseDouble(fields[3]);
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", normalizeName(name));
            item.put("price", Math.round(price * 100.0) / 100.0);
            item.put("change", Math.round(changePct * 100.0) / 100.0);
            result.add(item);
        }
        return result;
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s); }
        catch (Exception e) { return 0; }
    }

    private String normalizeName(String raw) {
        if (raw == null) return "";
        return raw
            .replace("标普500指数", "标普500")
            .replace("纳斯达克100", "纳斯达克100")
            .replace("恒生指数", "恒生指数")
            .replace("上证指数", "上证指数")
            .replace("创业板指", "创业板指");
    }

    /**
     * 个股实时行情（新浪财经 API）
     * GET /api/finance/stock-quotes?codes=sz002595,sh600885,gb_msft
     */
    @GetMapping("/stock-quotes")
    public ApiResponse<List<Map<String, Object>>> getStockQuotes(@RequestParam String codes) {
        String url = "http://hq.sinajs.cn/list=" + codes;
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Referer", "https://finance.sina.com.cn")
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return ApiResponse.success(parseStockQuotes(resp.body()));
        } catch (Exception e) {
            return ApiResponse.error(502, "无法连接新浪 API");
        }
    }

    private List<Map<String, Object>> parseStockQuotes(String body) {
        List<Map<String, Object>> result = new ArrayList<>();
        String[] lines = body.split("\n");
        for (String line : lines) {
            if (!line.contains("=")) continue;
            String codePart = line.substring(line.indexOf("hq_str_") + 7, line.indexOf("="));
            String data = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
            String[] fields = data.split(",");
            if (fields.length < 4 || fields[0].isEmpty()) continue;

            String name = fields[0];
            double price, prevClose, changePct;

            if (codePart.startsWith("gb_")) {
                // 美股: fields[1]=当前价(盘中)/昨收(休市), fields[2]=涨跌幅%
                price = parseDouble(fields[1]);
                changePct = parseDouble(fields[2]);
            } else {
                // A 股: fields[1]=开盘, fields[2]=昨收, fields[3]=当前价, fields[32]=涨跌幅%
                price = parseDouble(fields[3]);
                prevClose = parseDouble(fields[2]);
                changePct = prevClose > 0 ? ((price - prevClose) / prevClose) * 100 : 0;
            }

            // 休市时涨跌幅为 0，用缓存的上次非零值替代
            if (changePct == 0.0 && lastChangeCache.containsKey(codePart)) {
                changePct = lastChangeCache.get(codePart);
            } else if (changePct != 0.0) {
                lastChangeCache.put(codePart, changePct);
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", codePart);
            item.put("name", name);
            item.put("price", Math.round(price * 100.0) / 100.0);
            item.put("change", Math.round(changePct * 100.0) / 100.0);
            result.add(item);
        }
        return result;
    }
}
