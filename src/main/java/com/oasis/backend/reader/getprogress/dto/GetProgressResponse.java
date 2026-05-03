package com.oasis.backend.reader.getprogress.dto;

public record GetProgressResponse(
        String userId,
        String seriesId,
        String chapterId,
        Integer chapterNumber,
        Integer lastReadPage
) {
}
