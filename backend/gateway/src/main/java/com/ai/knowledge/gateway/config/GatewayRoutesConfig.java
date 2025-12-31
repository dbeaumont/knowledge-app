package com.ai.knowledge.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${services.rag.uri}")
    private String ragUri;

    @Value("${services.document.uri}")
    private String documentUri;

    @Value("${services.user.uri}")
    private String userUri;

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("rag", r -> r.path("/api/rag/**").uri(ragUri))
                .route("documents", r -> r.path("/api/documents/**").uri(documentUri))
                .route("users", r -> r.path("/api/users/**", "/api/auth/**").uri(userUri))
                .build();
    }
}
