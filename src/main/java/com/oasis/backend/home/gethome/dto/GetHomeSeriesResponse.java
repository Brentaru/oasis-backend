package com.oasis.backend.home.gethome.dto;

public record GetHomeSeriesResponse(
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
