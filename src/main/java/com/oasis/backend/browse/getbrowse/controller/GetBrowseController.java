package com.oasis.backend.browse.getbrowse.controller;

import com.oasis.backend.browse.getbrowse.service.GetBrowseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/browse")
public class GetBrowseController {

    private final GetBrowseService getBrowseService;

    public GetBrowseController(GetBrowseService getBrowseService) {
        this.getBrowseService = getBrowseService;
    }

    @GetMapping
    public ResponseEntity<?> getBrowse() {
        return getBrowseService.getBrowse();
    }
}
