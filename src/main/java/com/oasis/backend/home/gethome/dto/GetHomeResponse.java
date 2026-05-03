package com.oasis.backend.home.gethome.dto;

import java.util.List;

public record GetHomeResponse(
        GetHomeSeriesResponse featuredSeries,
        List<GetHomeSeriesResponse> recentUpdates,
        List<GetHomeContinueReadingResponse> continueReading
) {
}
