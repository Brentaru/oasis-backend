package com.oasis.backend.source.mangadex.dto;

public record MangaDexChapterNavigationResponse(
        String previousChapterId,
        Integer previousChapterNumber,
        String nextChapterId,
        Integer nextChapterNumber
) {
}
