package com.oasis.backend.source.mangadex.dto;

public record MangaDexChapterResponse(
        String chapterId,
        Integer chapterNumber,
        String title
) {
}
