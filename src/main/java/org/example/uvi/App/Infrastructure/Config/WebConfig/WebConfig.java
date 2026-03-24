package org.example.uvi.App.Infrastructure.Config.WebConfig;

import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Infrastructure.RateLimit.RateLimitInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/v1/auth/**", "/api/v1/users/**",
                        "/api/v1/families/**", "/api/v1/invitations/**",
                        "/api/v1/places/**", "/api/v1/devices/**",
                        "/api/v1/locations/**", "/api/v1/user/social/**",
                        "/api/v1/geocoding/**")
                .excludePathPatterns("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html");
    }
}
