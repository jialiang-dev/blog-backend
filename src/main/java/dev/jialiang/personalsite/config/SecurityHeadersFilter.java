package dev.jialiang.personalsite.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 为所有 HTTP 响应添加基本安全头
 */
@Component
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResp = (HttpServletResponse) response;

        // 防止 MIME 类型嗅探
        httpResp.setHeader("X-Content-Type-Options", "nosniff");
        // 防止点击劫持
        httpResp.setHeader("X-Frame-Options", "DENY");
        // 限制 referrer 泄露
        httpResp.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        // 禁止浏览器自动识别电话号码/地址
        httpResp.setHeader("X-Permitted-Cross-Domain-Policies", "none");

        chain.doFilter(request, response);
    }
}
