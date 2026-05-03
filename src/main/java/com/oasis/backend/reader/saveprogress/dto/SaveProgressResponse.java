package com.oasis.backend.reader.saveprogress.dto;

public record SaveProgressResponse(
        String userId,
        String seriesId,
        String chapterId,
        Integer chapterNumber,
        Integer lastReadPage,
        String message
) {
}
