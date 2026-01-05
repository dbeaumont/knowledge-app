package com.ai.knowledge.gateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RequestLoggingFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        String uri = request.getURI().toString();
        String headers = request.getHeaders().entrySet().stream()
                .map(e -> e.getKey() + "=" + String.join(",", e.getValue()))
                .reduce((a, b) -> a + "; " + b)
                .orElse("");

        log.info("API IN  {} {} headers=[{}]", method, uri, headers);

        return chain.filter(exchange)
                .doOnSuccess(ignored -> log.info("API OUT {} {} status={}", method, uri, exchange.getResponse().getStatusCode()))
                .doOnError(err -> log.error("API ERR {} {} : {}", method, uri, err.getMessage(), err));
    }
}
