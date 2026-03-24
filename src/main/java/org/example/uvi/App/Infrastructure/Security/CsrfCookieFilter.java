package org.example.uvi.App.Infrastructure.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр для SPA, который принудительно инициализирует CSRF-токен
 * и сохраняет его в cookie (XSRF-TOKEN).
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            // Принудительно инициализируем токен, чтобы он попал в CookieCsrfTokenRepository
            // В Spring Security 6 это необходимо для stateless приложений
            String token = csrfToken.getToken();
            
            // Если кука XSRF-TOKEN отсутствует в ответе (не была установлена репозиторием),
            // можно было бы установить её вручную, но CookieCsrfTokenRepository должен справляться сам
            // при вызове getToken() если мы используем наш requestHandler.
        }
        filterChain.doFilter(request, response);
    }
}
