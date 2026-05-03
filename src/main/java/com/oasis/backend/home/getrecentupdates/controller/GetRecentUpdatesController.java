package com.oasis.backend.home.getrecentupdates.controller;

import com.oasis.backend.home.getrecentupdates.service.GetRecentUpdatesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class GetRecentUpdatesController {

    private final GetRecentUpdatesService getRecentUpdatesService;

    public GetRecentUpdatesController(GetRecentUpdatesService getRecentUpdatesService) {
        this.getRecentUpdatesService = getRecentUpdatesService;
    }

    @GetMapping("/recent-updates")
    public ResponseEntity<?> getRecentUpdates() {
        return getRecentUpdatesService.getRecentUpdates();
    }
}
