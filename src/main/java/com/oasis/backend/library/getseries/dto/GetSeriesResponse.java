package com.oasis.backend.library.getseries.dto;

public record GetSeriesResponse(
        String seriesId,
        String title,
        String author,
        String coverImage,
        String genre,
        String status,
        Double rating
) {
}

