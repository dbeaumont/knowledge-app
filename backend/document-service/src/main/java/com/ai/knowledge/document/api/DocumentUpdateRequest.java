package com.ai.knowledge.document.api;

import jakarta.validation.constraints.NotBlank;

public record DocumentUpdateRequest(@NotBlank String name, String description) {}
