package com.ai.knowledge.document.api;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(UUID id, String name, String description, String originalFilename, String status,
                               Instant createdAt) {}
