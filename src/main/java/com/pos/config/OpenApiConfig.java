package com.pos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("POS System API")
                        .description("Multi-Tenant Point of Sale System with Inventory Management, Analytics, and SaaS Features")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("POS System Team")
                                .email("support@pos-system.com")
                                .url("https://pos-system.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.pos-system.com")
                                .description("Production Server"),
                        new Server()
                                .url("https://{tenant}.api.pos-system.com")
                                .description("Tenant-specific Production Server")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token for authentication. Include tenant ID in token for multi-tenant access.")))
                .security(List.of(new SecurityRequirement().addList("bearerAuth")));
    }
}
