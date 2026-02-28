package org.example.uvi.App.Infrastructure.RateLimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String key = getClientKey(request);
        Bucket bucket = cache.computeIfAbsent(key, k -> createBucket(request));

        if (bucket.tryConsume(1)) {
            response.addHeader("X-RateLimit-Remaining",
                    String.valueOf(bucket.getAvailableTokens()));
            return true;
        } else {
            response.addHeader("X-RateLimit-Retry-After-Seconds", "60");
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Too many requests. Please try again later.\",\"retryAfter\":60}");
            log.warn("Rate limit exceeded for key: {}, path: {}", key, request.getRequestURI());
            return false;
        }
    }

    private Bucket createBucket(HttpServletRequest request) {
        String path = request.getRequestURI();
        Bandwidth limit;

        if (path.contains("/auth/send-code") || path.contains("/auth/send")) {
            // SMS отправка: 3 попытки в 5 минут
            limit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(5)));
        } else if (path.contains("/auth/verify")) {
            // Верификация кода: 10 попыток в минуту
            limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        } else if (path.contains("/auth")) {
            // Прочие auth эндпоинты: 20 в минуту
            limit = Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1)));
        } else {
            // Общий лимит: 100 запросов в минуту
            limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        }

        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientKey(HttpServletRequest request) {
        return getClientIP(request) + ":" + request.getRequestURI();
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) return request.getRemoteAddr();
        return xfHeader.split(",")[0].trim();
    }
}
