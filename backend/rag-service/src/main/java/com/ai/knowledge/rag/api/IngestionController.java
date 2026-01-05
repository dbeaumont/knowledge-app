package com.ai.knowledge.rag.api;

import com.ai.knowledge.rag.service.RagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
}
