package com.oasis.backend.profile.editpassword.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.backend.profile.editpassword.dto.EditPasswordRequest;
import com.oasis.backend.profile.editpassword.dto.EditPasswordResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EditPasswordService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anonKey}")
    private String anonKey;

    @Value("${supabase.serviceRoleKey:}")
    private String serviceRoleKey;

    @Value("${supabase.profileTable:profiles}")
    private String profileTable;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpHeaders jsonHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", token);
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    public ResponseEntity<?> editPassword(String userId, EditPasswordRequest req) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body("User ID is required.");
        }
        if (req == null) {
            return ResponseEntity.badRequest().body("Request body is required.");
        }
        if (req.currentPassword() == null || req.currentPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Current password is required.");
        }
        if (req.newPassword() == null || req.newPassword().isBlank()) {
            return ResponseEntity.badRequest().body("New password is required.");
        }
        if (req.confirmPassword() == null || req.confirmPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Confirm password is required.");
        }
        if (!req.newPassword().equals(req.confirmPassword())) {
            return ResponseEntity.badRequest().body("New password and confirm password do not match.");
        }
        if (req.currentPassword().equals(req.newPassword())) {
            return ResponseEntity.badRequest().body("New password must be different from current password.");
        }

        if (serviceRoleKey == null || serviceRoleKey.isBlank()) {
            return ResponseEntity.status(500).body("Supabase service role key is required for password update.");
        }

        try {
            String email = findEmailByUserId(userId);
            if (email == null || email.isBlank()) {
                return ResponseEntity.status(404).body("Profile not found.");
            }

            if (!validateCurrentPassword(email, req.currentPassword())) {
                return ResponseEntity.status(401).body("Current password is incorrect.");
            }

            String url = supabaseUrl + "/auth/v1/admin/users/" + userId;

            Map<String, Object> body = new HashMap<>();
            body.put("password", req.newPassword());

            RestTemplate rt = new RestTemplate();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, jsonHeaders(serviceRoleKey));
            ResponseEntity<String> res = rt.exchange(url, HttpMethod.PUT, entity, String.class);

            if (!res.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(400).body("Failed to update password.");
            }

            return ResponseEntity.ok(new EditPasswordResponse("Password updated successfully."));

        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(404).body("User not found.");

        } catch (RestClientResponseException e) {
            return ResponseEntity.status(400).body("Failed to update password.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error. Please try again.");
        }
    }

    private String findEmailByUserId(String userId) {
        try {
            String url = supabaseUrl + "/rest/v1/" + profileTable
                    + "?user_id=eq." + userId
                    + "&select=email";

            RestTemplate rt = new RestTemplate();
            HttpEntity<Void> entity = new HttpEntity<>(jsonHeaders(anonKey));
            ResponseEntity<String> res = rt.exchange(url, HttpMethod.GET, entity, String.class);

            List<Map<String, Object>> rows = objectMapper.readValue(
                    res.getBody(),
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );

            if (rows.isEmpty()) {
                return null;
            }

            Object email = rows.get(0).get("email");
            return email == null ? null : email.toString();

        } catch (Exception e) {
            return null;
        }
    }

    private boolean validateCurrentPassword(String email, String currentPassword) {
        try {
            String url = supabaseUrl + "/auth/v1/token?grant_type=password";

            Map<String, Object> body = new HashMap<>();
            body.put("email", email);
            body.put("password", currentPassword);

            RestTemplate rt = new RestTemplate();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, jsonHeaders(anonKey));
            ResponseEntity<String> res = rt.postForEntity(url, entity, String.class);

            return res.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            return false;
        }
    }
}
