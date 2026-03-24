package org.example.uvi.App.Infrastructure.Config.SecurityConfig;

import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.servlet.http.HttpServletResponse;
import org.example.uvi.App.Infrastructure.Security.CsrfCookieFilter;
import org.example.uvi.App.Infrastructure.Security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        // Отключаем отложенную генерацию токена (deferred), чтобы он сразу попадал в куки
        // и отключаем XOR-кодирование для заголовков (используя стандартный обработчик)
        requestHandler.setCsrfRequestAttributeName(null);

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(tokenRepository)
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers(
                                "/api/v1/auth/send-code",
                                "/api/v1/auth/verify-code",
                                "/api/v1/auth/verify-2fa",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/csrf",
                                "/api/v1/auth/logout",
                                "/ws/**",
                                "/actuator/**"
                        )
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/send-code",
                                "/api/v1/auth/verify-code",
                                "/api/v1/auth/verify-2fa",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/csrf",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/**",
                                "/ws/**",
                                "/route-test.html",
                                "/static/**",
                                "/*.html",
                                "/*.css",
                                "/*.js"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);

        return http.build();
    }
}
