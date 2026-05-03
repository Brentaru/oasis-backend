package com.oasis.backend.home.continuereading.dto;

public record ContinueReadingResponse(
        String userId,
        String seriesId,
        String chapterId,
        Integer chapterNumber,
        Integer lastReadPage,
        String title,
        String author,
        String coverImage,
        String description,
        String genre,
        String status,
        Double rating
) {
}
