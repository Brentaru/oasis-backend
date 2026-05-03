package com.oasis.backend.library.getseries.controller;

import com.oasis.backend.library.getseries.service.GetSeriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library")
public class GetSeriesController {

    private final GetSeriesService getSeriesService;

    public GetSeriesController(GetSeriesService getSeriesService) {
        this.getSeriesService = getSeriesService;
    }

    @GetMapping("/series")
    public ResponseEntity<?> getSeries() {
        return getSeriesService.getSeries();
    }
}

