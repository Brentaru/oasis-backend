package com.oasis.backend.auth.login.service;

import com.oasis.backend.auth.login.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anonKey}")
    private String anonKey;

    private HttpHeaders supabaseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", anonKey);
        headers.set("Authorization", "Bearer " + anonKey);
        return headers;
    }

    public ResponseEntity<?> login(LoginRequest req) {

        if (req.email() == null || req.email().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }
        if (req.password() == null || req.password().isBlank()) {
            return ResponseEntity.badRequest().body("Password is required.");
        }

        String url = supabaseUrl + "/auth/v1/token?grant_type=password";

        Map<String, Object> body = new HashMap<>();
        body.put("email", req.email());
        body.put("password", req.password());

        try {
            RestTemplate rt = new RestTemplate();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, supabaseHeaders());
            ResponseEntity<String> res = rt.postForEntity(url, entity, String.class);
            return ResponseEntity.status(res.getStatusCode()).body(res.getBody());

        } catch (HttpClientErrorException.Unauthorized e) {
            return ResponseEntity.status(401).body("Invalid email or password.");

        } catch (HttpClientErrorException.BadRequest e) {
            String response = e.getResponseBodyAsString();
            if (response != null && response.toLowerCase().contains("invalid")) {
                return ResponseEntity.status(401).body("Invalid email or password.");
            }
            return ResponseEntity.status(400).body("Login failed. Please try again.");

        } catch (RestClientResponseException e) {
            return ResponseEntity.status(400).body("Login failed. Please try again.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error. Please try again.");
        }
    }
}