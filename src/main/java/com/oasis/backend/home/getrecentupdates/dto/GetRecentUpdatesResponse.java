package com.oasis.backend.home.getrecentupdates.dto;

public record GetRecentUpdatesResponse(
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
