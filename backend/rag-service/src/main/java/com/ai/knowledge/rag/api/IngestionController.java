package com.ai.knowledge.rag.api;

import com.ai.knowledge.rag.service.RagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;
@RestController
@RequestMapping("/api/rag")
public class IngestionController {

    private final RagService ragService;

    public IngestionController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/index")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void ingest(@Valid @RequestBody IngestRequest request) {
        ragService.ingest(request);
    }

    @DeleteMapping("/index/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void purge(@PathVariable UUID id) throws Exception {
        ragService.purge(id);
    }
}
