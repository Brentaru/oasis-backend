package com.oasis.backend.library.getchapters.dto;

public record GetChaptersResponse(
        String chapterId,
        Integer chapterNumber,
        String title
) {
}

