package com.oasis.backend.browse.getbrowse.dto;

public record GetBrowseResponse(
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
