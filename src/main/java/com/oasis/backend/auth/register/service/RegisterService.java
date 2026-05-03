package com.oasis.backend.auth.register.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.backend.auth.register.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class RegisterService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anonKey}")
    private String anonKey;

    @Value("${supabase.serviceRoleKey}")
    private String serviceRoleKey;

    @Value("${supabase.profileTable:profiles}")
    private String profileTable;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpHeaders supabaseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", anonKey);
        headers.set("Authorization", "Bearer " + anonKey);
        return headers;
    }

    private HttpHeaders profileHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", serviceRoleKey);
        headers.set("Authorization", "Bearer " + serviceRoleKey);
        return headers;
    }

    public ResponseEntity<?> register(RegisterRequest req) {

        if (req.email() == null || req.email().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }
        if (req.password() == null || req.password().isBlank()) {
            return ResponseEntity.badRequest().body("Password is required.");
        }
        if (req.confirmPassword() == null || req.confirmPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Confirm password is required.");
        }
        if (!req.password().equals(req.confirmPassword())) {
            return ResponseEntity.badRequest().body("Passwords do not match.");
        }

        String url = supabaseUrl + "/auth/v1/signup";

        Map<String, Object> body = new HashMap<>();
        body.put("email", req.email());
        body.put("password", req.password());

        try {
            RestTemplate rt = new RestTemplate();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, supabaseHeaders());
            ResponseEntity<String> res = rt.postForEntity(url, entity, String.class);

            String userId = extractUserId(res.getBody());
            if (userId == null || userId.isBlank()) {
                return ResponseEntity.status(500).body("Registration succeeded, but failed to create profile.");
            }

            boolean profileCreated = createDefaultProfile(rt, userId, req.email());
            if (!profileCreated) {
                return ResponseEntity.status(500).body("Registration succeeded, but failed to create profile.");
            }

            return ResponseEntity.status(res.getStatusCode()).body(res.getBody());

        } catch (HttpClientErrorException e) {
            String response = e.getResponseBodyAsString();

            if (response != null && response.toLowerCase().contains("user already registered")) {
                return ResponseEntity.status(409).body("Email already exists.");
            }

            return ResponseEntity.status(400).body("Registration failed. Please try again.");

        } catch (RestClientResponseException e) {
            return ResponseEntity.status(400).body("Registration failed. Please try again.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error. Please try again.");
        }
    }

    private String extractUserId(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("user").path("id").asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean createDefaultProfile(RestTemplate rt, String userId, String email) {
        String profileUrl = supabaseUrl + "/rest/v1/" + profileTable;

        Map<String, Object> profileBody = new HashMap<>();
        profileBody.put("user_id", userId);
        profileBody.put("email", email);
        profileBody.put("full_name", null);
        profileBody.put("profile_photo", null);

        try {
            HttpEntity<Map<String, Object>> profileEntity = new HttpEntity<>(profileBody, profileHeaders());
            rt.exchange(profileUrl, HttpMethod.POST, profileEntity, String.class);
            return true;
        } catch (RestClientResponseException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
