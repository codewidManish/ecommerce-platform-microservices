package com.ecommerce.userservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title       = "E-Commerce Platform — User Service",
        version     = "1.0.0",
        description = "JWT-secured User Service: Registration, Login, Token Refresh, RBAC",
        contact     = @Contact(name = "Platform Team", email = "platform@shopeasy.com"),
        license     = @License(name = "MIT", url  = "https://opensource.org/licenses/MIT")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "API Gateway (local)"),
        @Server(url = "http://localhost:8081", description = "User Service (direct)")
    }
)
@SecurityScheme(
    name         = "Bearer Authentication",
    type         = SecuritySchemeType.HTTP,
    scheme       = "bearer",
    bearerFormat = "JWT",
    in           = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // Annotation-driven — no bean methods needed
}
