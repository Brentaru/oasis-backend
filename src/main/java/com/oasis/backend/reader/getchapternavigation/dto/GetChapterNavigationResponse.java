package com.oasis.backend.reader.getchapternavigation.dto;

public record GetChapterNavigationResponse(
        String previousChapterId,
        Integer previousChapterNumber,
        String nextChapterId,
        Integer nextChapterNumber
) {
}
