package com.oasis.backend.source.mangadex.dto;

public record MangaDexSeriesResponse(
        String seriesId,
        String source,
        String sourceId,
        String title,
        String author,
        String artist,
        String coverImage,
        String genre,
        java.util.List<String> genres,
        String status,
        String contentRating,
        Integer year,
        Double rating
) {
}
