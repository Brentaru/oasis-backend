package com.oasis.backend.reader.saveprogress.dto;

public record SaveProgressRequest(
        String chapterId,
        Integer chapterNumber,
        Integer lastReadPage
) {
}
