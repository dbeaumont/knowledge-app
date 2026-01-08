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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository repository;
    private final DocumentIngestionClient ingestionClient;
    private final DocumentMapper mapper;
    private final PdfTextExtractor pdfTextExtractor;
    private final EpubTextExtractor epubTextExtractor;
    private final TextExtractor textExtractor;

    public DocumentService(DocumentRepository repository,
                           DocumentIngestionClient ingestionClient,
                           DocumentMapper mapper,
                           PdfTextExtractor pdfTextExtractor,
                           EpubTextExtractor epubTextExtractor,
                           TextExtractor textExtractor) {
        this.repository = repository;
        this.ingestionClient = ingestionClient;
        this.mapper = mapper;
        this.pdfTextExtractor = pdfTextExtractor;
        this.epubTextExtractor = epubTextExtractor;
        this.textExtractor = textExtractor;
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
    public DocumentResponse createFromUpload(MultipartFile file,
                                             String content,
                                             String name,
                                             String description,
                                             Authentication authentication) {
        boolean hasFile = file != null && !file.isEmpty();
        boolean hasContent = content != null && !content.isBlank();
        if (!hasFile && !hasContent) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier ou contenu manquant.");
        }

        String filename = hasFile ? file.getOriginalFilename() : null;
        String resolvedName = (name == null || name.isBlank()) ? filename : name;
        if (resolvedName == null || resolvedName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom du document est requis.");
        }

        String resolvedContent = hasContent ? content : extractContentFromFile(file);
        if (resolvedContent == null || resolvedContent.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le document ne contient pas de texte exploitable.");
        }

        DocumentRequest request = new DocumentRequest(resolvedName, description, resolvedContent);
        return create(request, authentication);
    }

    private String extractContentFromFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        boolean isPdf = "application/pdf".equalsIgnoreCase(contentType)
                || (filename != null && filename.toLowerCase(Locale.ROOT).endsWith(".pdf"));
        boolean isEpub = "application/epub+zip".equalsIgnoreCase(contentType)
                || (filename != null && filename.toLowerCase(Locale.ROOT).endsWith(".epub"));
        boolean isText = (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("text/"))
                || (filename != null && filename.toLowerCase(Locale.ROOT).endsWith(".txt"));
        if (!isPdf && !isText && !isEpub) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Seuls les fichiers PDF, EPUB ou texte sont acceptes.");
        }
        try {
            if (isPdf) {
                return pdfTextExtractor.extract(file);
            }
            if (isEpub) {
                return epubTextExtractor.extract(file);
            }
            return textExtractor.extract(file);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de lire le fichier.", e);
        }
    }

    @Transactional
    public void markIndexed(UUID id, String status) {
        repository.findById(id).ifPresent(doc -> {
            doc.setStatus(status);
            repository.save(doc);
        });
    }

    @Transactional
    public void delete(UUID id, Authentication authentication) {
        String ownerId = authentication.getName();
        var existing = repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable."));
        repository.delete(existing);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                ingestionClient.purge(id);
            }
        });
    }
}
