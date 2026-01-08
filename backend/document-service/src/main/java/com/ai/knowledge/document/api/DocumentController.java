package com.ai.knowledge.document.api;

import com.ai.knowledge.document.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public List<DocumentResponse> list(Authentication authentication) {
        return documentService.list(authentication);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse create(@RequestPart(value = "file", required = false) MultipartFile file,
                                   @RequestPart(value = "content", required = false) String content,
                                   @RequestPart(value = "name", required = false) String name,
                                   @RequestPart(value = "description", required = false) String description,
                                   Authentication authentication) {
        return documentService.createFromUpload(file, content, name, description, authentication);
    }

    @PostMapping("/{id}/status")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateStatus(@PathVariable UUID id, @RequestParam String status) {
        documentService.markIndexed(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, Authentication authentication) {
        documentService.delete(id, authentication);
    }
}
