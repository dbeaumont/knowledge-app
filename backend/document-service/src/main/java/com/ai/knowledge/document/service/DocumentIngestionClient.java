package com.ai.knowledge.document.service;

import com.ai.knowledge.document.domain.DocumentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class DocumentIngestionClient {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionClient.class);

    private final RestTemplate restTemplate;
    private final String ingestionUrl;

    public DocumentIngestionClient(RestTemplate restTemplate,
                                   @Value("${ingestion.rag-url:http://rag-service:8080/api/rag/index}") String ingestionUrl) {
        this.restTemplate = restTemplate;
        this.ingestionUrl = ingestionUrl;
    }

    public void ingest(DocumentEntity document) {
        if (document.getContent() == null || document.getContent().isBlank()) {
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            var payload = new IngestPayload(document.getId(), document.getName(), document.getDescription(), document.getContent());
            restTemplate.postForEntity(ingestionUrl, new HttpEntity<>(payload, headers), Void.class);
        } catch (Exception e) {
            log.warn("Unable to ingest document {} to rag-service: {}", document.getId(), e.getMessage());
        }
    }

    public record IngestPayload(UUID id, String name, String description, String content) {}
}
