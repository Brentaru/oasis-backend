package com.oasis.backend.home.gethome.controller;

import com.oasis.backend.home.gethome.service.GetHomeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class GetHomeController {

    private final GetHomeService getHomeService;

    public GetHomeController(GetHomeService getHomeService) {
        this.getHomeService = getHomeService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getHome(@PathVariable String userId) {
        return getHomeService.getHome(userId);
    }
}
