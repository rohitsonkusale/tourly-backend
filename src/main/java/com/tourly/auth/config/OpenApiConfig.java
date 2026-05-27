package com.tourly.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tourlyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tourly API")
                        .description("Tourly Backend APIs for authentication, trips, bookings, payments, admin, and user profiles.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Tourly Backend Team")
                                .email("support@tourly.com")))
                .schemaRequirement(
                        "bearerAuth",
                        new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                );
    }
}