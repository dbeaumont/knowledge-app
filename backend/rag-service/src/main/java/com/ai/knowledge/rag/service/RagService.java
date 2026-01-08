package com.ai.knowledge.rag.service;

import com.ai.knowledge.rag.api.IngestRequest;
import com.ai.knowledge.rag.api.RagRequest;
import com.ai.knowledge.rag.api.RagResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RagService {

    @Value("${spring.ai.openai.chat.options.model:qwen2.5}")
    private String defaultModel;

    @Value("${rag.ingest.chunk-size:256}")
    private int ingestChunkSize;

    @Value("${rag.ingest.min-chars:200}")
    private int ingestMinChunkChars;

    @Value("${rag.ingest.min-chunk-length:5}")
    private int ingestMinChunkLength;

    @Value("${rag.ingest.max-chunks:1000}")
    private int ingestMaxChunks;

    @Value("${rag.ingest.keep-separator:true}")
    private boolean ingestKeepSeparator;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private TokenTextSplitter textSplitter;

    public RagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    void initSplitter() {
        this.textSplitter = new TokenTextSplitter(
                ingestChunkSize,
                ingestMinChunkChars,
                ingestMinChunkLength,
                ingestMaxChunks,
                ingestKeepSeparator
        );
    }

    public Flux<String> streamAnswer(RagRequest request) {
        String systemPrompt = buildPrompt(request.query());
        return chatClient.prompt()
                .system(systemPrompt)
                .user(request.query())
                .stream()
                .content();
    }

    public Mono<RagResponse> answer(RagRequest request) {
        String conversationId = request.conversationId() != null ? request.conversationId() : UUID.randomUUID().toString();
        String systemPrompt = buildPrompt(request.query());
        return Mono.fromSupplier(() -> chatClient.prompt()
                        .system(systemPrompt)
                        .user(request.query())
                        .call()
                        .content())
                .map(answer -> new RagResponse(answer, defaultModel, List.of("openai"), conversationId));
    }

    public void ingest(IngestRequest request) {
        Document doc = new Document(
                request.content(),
                java.util.Map.of(
                        "id", request.id() != null ? request.id().toString() : null,
                        "name", request.name(),
                        "description", request.description()
                )
        );
        List<Document> chunks = textSplitter.split(doc);
        for (int i = 0; i < chunks.size(); i++) {
            chunks.get(i).getMetadata().put("chunk", i);
            chunks.get(i).getMetadata().put("chunkCount", chunks.size());
        }
        vectorStore.add(chunks);
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
