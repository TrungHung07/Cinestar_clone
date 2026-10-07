package com.cinestar.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cau hinh Swagger UI (springdoc-openapi).
 *
 * Truy cap: http://localhost:8090/swagger-ui.html
 * Nhan "Authorize" va dan access token (khong can tien to "Bearer") de test
 * cac endpoint can dang nhap/quyen ADMIN.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI cinestarOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cinestar Cinema API")
                        .description("""
                                API hệ thống đặt vé xem phim clone cinestar.com.vn \
                                — Spring Boot MVC, PostgreSQL, Redis, Kafka, VNPAY Sandbox.\
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("Cinestar Dev").email("dev@cinestar.vn"))
                        .license(new License().name("MIT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Dán access token từ /api/v1/auth/login")));
    }
}
