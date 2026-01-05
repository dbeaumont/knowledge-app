package com.ai.knowledge.rag.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String headers = Collections.list(request.getHeaderNames()).stream()
                .map(name -> name + "=" + Collections.list(request.getHeaders(name)).stream().collect(Collectors.joining(",")))
                .collect(Collectors.joining("; "));

        log.info("API IN  {} {} headers=[{}]", method, uri, headers);
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            log.error("API ERR {} {} : {}", method, uri, ex.getMessage(), ex);
            throw ex;
        }
        log.info("API OUT {} {} status={}", method, uri, response.getStatus());
    }
}
