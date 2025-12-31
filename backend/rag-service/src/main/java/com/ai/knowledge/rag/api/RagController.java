package com.ai.knowledge.rag.api;

import com.ai.knowledge.rag.service.RagService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping(value = "/query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamAnswer(@Valid @RequestBody RagRequest request) {
        return ragService.streamAnswer(request);
    }

    @PostMapping("/answer")
    public Mono<RagResponse> blockingAnswer(@Valid @RequestBody RagRequest request) {
        return ragService.answer(request);
    }
}
