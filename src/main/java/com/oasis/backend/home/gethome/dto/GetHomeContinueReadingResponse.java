package com.oasis.backend.home.gethome.dto;

public record GetHomeContinueReadingResponse(
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
