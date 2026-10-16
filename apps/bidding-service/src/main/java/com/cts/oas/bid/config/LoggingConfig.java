package com.cts.oas.bid.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class LoggingConfig extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingConfig.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);

        long startTime = System.currentTimeMillis();

        String method = request.getMethod();
        String uri    = request.getRequestURI();

        log.info(">>> INCOMING REQUEST  [{} {}] | requestId={}", method, uri, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int  status   = response.getStatus();

            if (status >= 500) {
                log.error("<<< RESPONSE [{} {}] | status={} | duration={}ms | requestId={}",
                        method, uri, status, duration, requestId);
            } else if (status >= 400) {
                log.warn("<<< RESPONSE  [{} {}] | status={} | duration={}ms | requestId={}",
                        method, uri, status, duration, requestId);
            } else {
                log.info("<<< RESPONSE  [{} {}] | status={} | duration={}ms | requestId={}",
                        method, uri, status, duration, requestId);
            }

            MDC.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Skip logging for Swagger UI and actuator endpoints to reduce noise
        return uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/actuator");
    }
}