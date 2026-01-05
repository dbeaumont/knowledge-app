package com.ai.knowledge.rag.service;

import com.ai.knowledge.rag.api.IngestRequest;
import com.ai.knowledge.rag.api.RagRequest;
import com.ai.knowledge.rag.api.RagResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RagService {

    @Value("${spring.ai.ollama.chat.model:llama3.1:8b}")
    private String defaultModel;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    public Flux<String> streamAnswer(RagRequest request) {
        String systemPrompt = buildPrompt(request.query());
        return chatClient.prompt()
//                .options(OllamaOptions.create().withModel(defaultModel))
                .system(systemPrompt)
                .user(request.query())
                .stream()
                .content();
    }

    public Mono<RagResponse> answer(RagRequest request) {
        String conversationId = request.conversationId() != null ? request.conversationId() : UUID.randomUUID().toString();
        String systemPrompt = buildPrompt(request.query());
        return Mono.fromSupplier(() -> chatClient.prompt()
//                        .options(OllamaOptions.create().withModel(defaultModel))
                        .system(systemPrompt)
                        .user(request.query())
                        .call()
                        .content())
                .map(answer -> new RagResponse(answer, defaultModel, List.of("ollama"), conversationId));
    }

    public void ingest(IngestRequest request) {
        Document doc = new Document(
                request.content(),
                java.util.Map.of(
                        "id", request.id(),
                        "name", request.name(),
                        "description", request.description()
                )
        );
        vectorStore.add(List.of(doc));
    }

    private String buildPrompt(String query) {
        String context = searchContext(query);
        return """
                Contexte:
                %s

                Tu dois répondre en français en t'appuyant STRICTEMENT sur le contexte fourni.
                Si tu ne sais pas, réponds "Je n'ai pas trouvé l'information dans les documents."
                """.formatted(context);
    }

    private String searchContext(String query) {
        List<Document> docs = vectorStore.similaritySearch(SearchRequest.query(query).withTopK(4));
        if (docs.isEmpty()) {
            return "Aucun contexte disponible.";
        }
        return docs.stream()
                .map(doc -> "- " + doc.getContent())
                .collect(Collectors.joining("\n"));
    }
}
