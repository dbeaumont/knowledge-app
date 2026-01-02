package com.ai.knowledge.document.api;

import jakarta.validation.constraints.NotBlank;

public record DocumentRequest(@NotBlank String name, String description, String content) {}
