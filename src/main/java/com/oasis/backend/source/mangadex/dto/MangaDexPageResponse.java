package com.oasis.backend.source.mangadex.dto;

public record MangaDexPageResponse(
        String pageId,
        Integer pageNumber,
        String imageUrl
) {
}
