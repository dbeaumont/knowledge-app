package com.ai.knowledge.document.api;

import com.ai.knowledge.document.domain.DocumentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", source = "ownerId")
    @Mapping(target = "status", constant = "NEW")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    DocumentEntity toEntity(DocumentRequest request, String ownerId);

    DocumentResponse toResponse(DocumentEntity entity);
}
