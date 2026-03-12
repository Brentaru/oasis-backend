package com.oasis.backend.auth.login.controller;

import com.oasis.backend.auth.login.dto.LoginRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.oasis.backend.auth.login.service.LoginService;


@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return loginService.login(req);
    }

}