package com.yourdomain.personalsite.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourdomain.personalsite.dto.ApiResponse;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 新浪财经实时行情 API（免费，无需 key）
    private static final String SINA_URL =
        "http://hq.sinajs.cn/list=s_sh000001,s_sz399001,s_sh000300,s_sz399006," +
        "int_dji,int_nasdaq,int_sp500,int_nikkei,rt_hkHSI,int_kospi";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    @GetMapping("/indices")
    public ApiResponse<List<Map<String, Object>>> getIndices() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(SINA_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("Referer", "https://finance.sina.com.cn")
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return ApiResponse.success(parseSinaResponse(resp.body()));
        } catch (Exception e) {
            // 返回降级数据
            return ApiResponse.success(fallbackData());
        }
    }

    private List<Map<String, Object>> parseSinaResponse(String body) {
        List<Map<String, Object>> result = new ArrayList<>();
        String[] lines = body.split("\n");
        for (String line : lines) {
            if (!line.contains("=")) continue;
            // 判断是否为港股实时数据（rt_ 前缀），其字段位置与 A 股/国际指数不同
            boolean isHK = line.contains("hq_str_rt_");
            String data = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
            String[] fields = data.split(",");
            if (fields.length < 4) continue;

            String name;
            double price;
            double changePct;

            if (isHK) {
                // 港股格式: 英文简称,中文名,当前价,昨收,最高,最低,开盘,涨跌额,涨跌幅%,...
                name = fields[1];                    // 中文名称
                price = parseDouble(fields[2]);      // 当前价
                changePct = parseDouble(fields[8]);  // 涨跌幅%
            } else {
                // A股/国际指数格式: 名称,当前价,涨跌额,涨跌幅%,...
                name = fields[0];
                price = parseDouble(fields[1]);
                changePct = parseDouble(fields[3]);
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("price", Math.round(price * 100.0) / 100.0);
            item.put("change", Math.round(changePct * 100.0) / 100.0);
            result.add(item);
        }
        return result.isEmpty() ? fallbackData() : result;
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s); }
        catch (Exception e) { return 0; }
    }

    private List<Map<String, Object>> fallbackData() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[][] fb = {
            {"上证指数", "3258.36"}, {"深证成指", "10892.71"}, {"沪深300", "3891.54"},
            {"创业板指", "2156.30"}, {"标普500", "5302.15"}, {"纳斯达克", "18972.45"},
            {"道琼斯", "42081.33"}, {"日经225", "38710.52"}, {"恒生指数", "19463.87"},
            {"韩国KOSPI", "2735.18"}
        };
        for (String[] f : fb) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", f[0]);
            item.put("price", Double.parseDouble(f[1]));
            item.put("change", 0.0);
            list.add(item);
        }
        return list;
    }

    /**
     * 个股日K线数据（新浪财经 API）
     * GET /api/finance/kline?code=sz002595&days=30
     */
    @GetMapping("/kline")
    public ApiResponse<List<Map<String, Object>>> getKline(@RequestParam String code, @RequestParam(defaultValue = "30") int days) {
        // 判断A股还是美股
        String url;
        if (code.startsWith("gb_")) {
            // 美股用新浪日K接口
            url = String.format("http://stock.finance.sina.com.cn/usstock/api/json_v2.php/US_MinKService.getDailyK?symbol=%s&datalen=%d",
                    code.replace("gb_", ""), days);
        } else {
            url = String.format("http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=%s&scale=240&ma=no&datalen=%d",
                    code, days);
        }
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Referer", "https://finance.sina.com.cn")
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return ApiResponse.success(parseKline(resp.body()));
        } catch (Exception e) {
            return ApiResponse.success(List.of());
        }
    }

    private List<Map<String, Object>> parseKline(String body) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            JsonNode arr = objectMapper.readTree(body);
            for (JsonNode node : arr) {
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("date", node.get("day").asText());
                point.put("close", parseDouble(node.get("close").asText()));
                result.add(point);
            }
        } catch (Exception e) {
            return result;
        }
        return result;
    }

    /**
     * 个股实时行情（新浪财经 API）
     * GET /api/finance/stock-quotes?codes=sz002595,sh600885,gb_msft
     */
    @GetMapping("/stock-quotes")
    public ApiResponse<List<Map<String, Object>>> getStockQuotes(@RequestParam String codes) {
        String sinaCodes = codes.replace("gb_", "gb_"); // 美股前缀保持不变
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
            return ApiResponse.success(List.of());
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
                // 美股: fields[1]=当前价, fields[26]=昨收
                price = parseDouble(fields[1]);
                prevClose = fields.length > 26 ? parseDouble(fields[26]) : price;
                changePct = prevClose > 0 ? ((price - prevClose) / prevClose) * 100 : 0;
            } else {
                // A 股: fields[1]=开盘, fields[2]=昨收, fields[3]=当前价
                price = parseDouble(fields[3]);
                prevClose = parseDouble(fields[2]);
                changePct = prevClose > 0 ? ((price - prevClose) / prevClose) * 100 : 0;
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
