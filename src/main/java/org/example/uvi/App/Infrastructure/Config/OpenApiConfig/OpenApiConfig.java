package org.example.uvi.App.Infrastructure.Config.OpenApiConfig;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.url:http://localhost:8888}")
    private String serverUrl;

    @Bean
    public OpenAPI uviOpenAPI() {
        Server server = new Server();
        server.setUrl(serverUrl);
        server.setDescription("UVI API Server");

        Contact contact = new Contact();
        contact.setName("UVI Team");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("UVI API")
                .version("1.0.0")
                .description("""
                        ## REST API для UVI
                        
                        **Возможности:**
                        - 👨‍👩‍👧‍👦 Управление семьями и приглашениями
                        - 📍 Места с геолокацией (PostGIS)
                        - 🏷️ Теги и интересы пользователей
                        - 📱 SMS-аутентификация + JWT
                        - 📡 Real-time геолокация через MQTT
                        - 📲 Управление устройствами (push-уведомления)
                        - 🔒 Rate Limiting (Bucket4j)
                        - ⚡ Virtual Threads (Project Loom)
                        """)
                .contact(contact)
                .license(license);

        SecurityScheme securityScheme = new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Введите JWT токен полученный через /api/v1/auth/verify-code");

        return new OpenAPI()
                .info(info)
                .servers(List.of(server))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme));
    }
}
