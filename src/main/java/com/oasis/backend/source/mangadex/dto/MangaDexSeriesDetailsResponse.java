package com.oasis.backend.source.mangadex.dto;

public record MangaDexSeriesDetailsResponse(
        String seriesId,
        String source,
        String sourceId,
        String title,
        String author,
        String artist,
        String coverImage,
        String description,
        String genre,
        java.util.List<String> genres,
        String status,
        String contentRating,
        Integer year,
        Double rating,
        Integer totalChapters,
        Integer latestChapterNumber
) {
}
