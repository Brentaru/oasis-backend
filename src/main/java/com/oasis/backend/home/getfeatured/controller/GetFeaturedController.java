package com.oasis.backend.home.getfeatured.controller;

import com.oasis.backend.home.getfeatured.service.GetFeaturedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class GetFeaturedController {

    private final GetFeaturedService getFeaturedService;

    public GetFeaturedController(GetFeaturedService getFeaturedService) {
        this.getFeaturedService = getFeaturedService;
    }

    @GetMapping("/featured")
    public ResponseEntity<?> getFeatured() {
        return getFeaturedService.getFeatured();
    }
}
