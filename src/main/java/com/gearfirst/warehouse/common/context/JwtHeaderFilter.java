package com.gearfirst.warehouse.common.context;

import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.stereotype.Component;

/**
 * Reads X-User-* headers (provided by API Gateway after JWT validation),
 * decodes Base64-encoded fields, and stores them into ThreadLocal {@link UserContextHolder}.
 */
@Component
public class JwtHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) servletRequest;

        String userId = httpReq.getHeader("X-User-Id");
        // Support both variants: X-User-Name and X-Username, etc.
        String name = decode(firstNonBlank(httpReq.getHeader("X-User-Name"), httpReq.getHeader("X-Username")));
        String rank = decode(firstNonBlank(httpReq.getHeader("X-User-Rank"), httpReq.getHeader("X-Rank")));
        String region = decode(firstNonBlank(httpReq.getHeader("X-User-Region"), httpReq.getHeader("X-Region")));
        String workType = decode(firstNonBlank(httpReq.getHeader("X-User-WorkType"), httpReq.getHeader("X-WorkType")));

        if (userId != null) {
            UserContextHolder.set(new UserContext(userId, name, rank, region, workType));
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContextHolder.clear();
        }
    }

    private String decode(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // Malformed Base64 header → treat as invalid token/header
            throw new BadRequestException(ErrorStatus.INVALID_TOKEN);
        }
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return a != null ? a : b;
    }
}
