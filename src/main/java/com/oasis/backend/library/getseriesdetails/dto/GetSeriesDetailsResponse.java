package com.oasis.backend.library.getseriesdetails.dto;

public record GetSeriesDetailsResponse(
        String seriesId,
        String title,
        String author,
        String coverImage,
        String description,
        String genre,
        String status,
        Double rating,
        Integer totalChapters,
        Integer latestChapterNumber
) {
}

