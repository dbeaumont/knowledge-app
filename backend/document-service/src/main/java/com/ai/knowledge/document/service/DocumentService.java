package com.ai.knowledge.document.service;

import com.ai.knowledge.document.api.DocumentMapper;
import com.ai.knowledge.document.api.DocumentRequest;
import com.ai.knowledge.document.api.DocumentResponse;
import com.ai.knowledge.document.domain.DocumentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository repository;
    private final DocumentIngestionClient ingestionClient;
    private final DocumentMapper mapper;

    public DocumentService(DocumentRepository repository, DocumentIngestionClient ingestionClient, DocumentMapper mapper) {
        this.repository = repository;
        this.ingestionClient = ingestionClient;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> list(Authentication authentication) {
        String ownerId = authentication.getName();
        return repository.findByOwnerId(ownerId).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public DocumentResponse create(DocumentRequest request, Authentication authentication) {
        String ownerId = authentication.getName();
        var entity = mapper.toEntity(request, ownerId);
        var saved = repository.save(entity);
        // Ingestion HTTP vers rag-service déclenchée après commit pour libérer la connexion DB rapidement
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                ingestionClient.ingest(saved);
            }
        });
        return mapper.toResponse(saved);
    }

    @Transactional
    public void markIndexed(UUID id, String status) {
        repository.findById(id).ifPresent(doc -> {
            doc.setStatus(status);
            repository.save(doc);
        });
    }
}
