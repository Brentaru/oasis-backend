package com.oasis.backend.home.getfeatured.dto;

public record GetFeaturedResponse(
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
