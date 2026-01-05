package com.ai.knowledge.rag.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IngestRequest(@NotNull UUID id,
                            @NotBlank String name,
                            String description,
                            @NotBlank String content) {
}
