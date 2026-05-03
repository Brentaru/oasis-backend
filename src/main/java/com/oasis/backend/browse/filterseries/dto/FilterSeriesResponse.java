package com.oasis.backend.browse.filterseries.dto;

public record FilterSeriesResponse(
        String seriesId,
        String title,
        String author,
        String coverImage,
        String description,
        String genre,
        String status,
        Double rating
) {
}
