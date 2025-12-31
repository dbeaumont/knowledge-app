package com.ai.knowledge.rag.service;

import com.ai.knowledge.rag.api.RagRequest;
import com.ai.knowledge.rag.api.RagResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class RagService {

    @Value("${llm.model:llama3}")
    private String defaultModel;

    public Flux<String> streamAnswer(RagRequest request) {
        // Placeholder streaming response while wiring Ollama streaming
        return Flux.just("Thinking...", "Synthesizing answer", "Done")
                .delayElements(Duration.ofMillis(250));
    }

    public Mono<RagResponse> answer(RagRequest request) {
        String conversationId = request.conversationId() != null ? request.conversationId() : UUID.randomUUID().toString();
        String answer = "RAG response placeholder for: " + request.query();
        return Mono.just(new RagResponse(answer, defaultModel, List.of("local-memory"), conversationId));
    }
}
