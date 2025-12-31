package com.ai.knowledge.rag.api;

import java.util.List;

public record RagResponse(String answer, String model, List<String> sources, String conversationId) {}
