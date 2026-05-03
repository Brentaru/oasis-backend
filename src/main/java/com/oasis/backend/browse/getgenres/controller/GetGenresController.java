package com.oasis.backend.browse.getgenres.controller;

import com.oasis.backend.browse.getgenres.service.GetGenresService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/browse")
public class GetGenresController {

    private final GetGenresService getGenresService;

    public GetGenresController(GetGenresService getGenresService) {
        this.getGenresService = getGenresService;
    }

    @GetMapping("/genres")
    public ResponseEntity<?> getGenres() {
        return getGenresService.getGenres();
    }
}
