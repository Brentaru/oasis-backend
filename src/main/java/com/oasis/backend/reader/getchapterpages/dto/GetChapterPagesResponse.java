package com.oasis.backend.reader.getchapterpages.dto;

public record GetChapterPagesResponse(
        String pageId,
        Integer pageNumber,
        String imageUrl
) {
}
