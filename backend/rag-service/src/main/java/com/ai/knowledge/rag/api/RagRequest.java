package com.ai.knowledge.rag.api;

import jakarta.validation.constraints.NotBlank;

public record RagRequest(@NotBlank String query, String conversationId) {}
