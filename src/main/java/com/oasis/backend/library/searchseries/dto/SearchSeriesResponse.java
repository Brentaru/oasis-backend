package com.oasis.backend.library.searchseries.dto;

public record SearchSeriesResponse(
        String seriesId,
        String title,
        String author,
        String coverImage,
        String genre,
        String status,
        Double rating
) {
}

