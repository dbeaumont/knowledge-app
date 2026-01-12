package com.ai.knowledge.document.service;

import com.ai.knowledge.document.domain.DocumentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class DocumentIngestionClient {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionClient.class);
    private static final Authentication SERVICE_PRINCIPAL =
            new AnonymousAuthenticationToken("rag-ingestor", "rag-ingestor", AuthorityUtils.createAuthorityList("ROLE_SERVICE"));

    private final RestTemplate restTemplate;
    private final OAuth2AuthorizedClientManager clientManager;
    private final String registrationId;
    private final String ingestionUrl;
    private final String purgeUrl;

    public DocumentIngestionClient(RestTemplate restTemplate,
                                   OAuth2AuthorizedClientManager clientManager,
                                   @Value("${ingestion.oauth2.registration-id:rag-ingestor}") String registrationId,
                                   @Value("${ingestion.rag-url:http://rag-service:8080/api/rag/index}") String ingestionUrl,
                                   @Value("${ingestion.rag-delete-url:http://rag-service:8080/api/rag/index}") String purgeUrl) {
        this.restTemplate = restTemplate;
        this.clientManager = clientManager;
        this.registrationId = registrationId;
        this.ingestionUrl = ingestionUrl;
        this.purgeUrl = purgeUrl;
    }

    public void ingest(DocumentEntity document) {
        if (document.getContent() == null || document.getContent().isBlank()) {
            return;
        }
        try {
            String accessToken = accessToken();
            if (accessToken == null) {
                return;
            }
            HttpHeaders headers = bearerHeaders(accessToken);
            var payload = new IngestPayload(document.getId(), document.getName(), document.getDescription(), document.getContent());
            restTemplate.postForEntity(ingestionUrl, new HttpEntity<>(payload, headers), Void.class);
        } catch (Exception e) {
            log.warn("Unable to ingest document {} to rag-service: {}", document.getId(), e.getMessage());
        }
    }

    public void purge(UUID documentId) {
        try {
            String accessToken = accessToken();
            if (accessToken == null) {
                return;
            }
            HttpHeaders headers = bearerHeaders(accessToken);
            restTemplate.exchange(purgeUrl + "/" + documentId, org.springframework.http.HttpMethod.DELETE,
                    new HttpEntity<>(headers), Void.class);
        } catch (Exception e) {
            log.warn("Unable to purge document {} from rag-service: {}", documentId, e.getMessage());
        }
    }

    private String accessToken() {
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest.withClientRegistrationId(registrationId)
                .principal(SERVICE_PRINCIPAL)
                .build();
        OAuth2AuthorizedClient client = clientManager.authorize(request);
        if (client == null || client.getAccessToken() == null) {
            log.warn("Unable to obtain client credentials token for rag-service");
            return null;
        }
        return client.getAccessToken().getTokenValue();
    }

    private HttpHeaders bearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }

    public record IngestPayload(UUID id, String name, String description, String content) {}
}
