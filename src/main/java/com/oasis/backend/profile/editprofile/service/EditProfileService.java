package com.oasis.backend.profile.editprofile.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.backend.profile.editprofile.dto.EditProfileRequest;
import com.oasis.backend.profile.editprofile.dto.EditProfileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EditProfileService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.serviceRoleKey}")
    private String serviceRoleKey;

    @Value("${supabase.profileTable:profiles}")
    private String profileTable;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpHeaders supabaseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", serviceRoleKey);
        headers.set("Authorization", "Bearer " + serviceRoleKey);
        headers.set("Prefer", "return=representation");
        return headers;
    }

    public ResponseEntity<?> editProfile(String userId, EditProfileRequest req) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body("User ID is required.");
        }
        if (req == null) {
            return ResponseEntity.badRequest().body("Request body is required.");
        }

        Map<String, Object> body = new HashMap<>();

        if (req.fullName() != null && !req.fullName().isBlank()) {
            body.put("full_name", req.fullName());
        }
        if (req.email() != null && !req.email().isBlank()) {
            body.put("email", req.email());
        }
        if (body.isEmpty()) {
            return ResponseEntity.badRequest().body("At least one profile field is required.");
        }

        String url = supabaseUrl + "/rest/v1/" + profileTable + "?user_id=eq." + userId;

        try {
            RestTemplate rt = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, supabaseHeaders());
            ResponseEntity<String> res = rt.exchange(url, HttpMethod.PATCH, entity, String.class);

            String responseBody = res.getBody();

            if (responseBody == null || responseBody.isBlank()) {
                return ResponseEntity.ok("Profile updated successfully.");
            }

            List<Map<String, Object>> rows = objectMapper.readValue(
                    responseBody,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            if (rows.isEmpty()) {
                return ResponseEntity.status(404).body("Profile not found.");
            }

            Map<String, Object> row = rows.get(0);

            EditProfileResponse response = new EditProfileResponse(
                    value(row.get("user_id")),
                    value(row.get("full_name")),
                    value(row.get("email")),
                    value(row.get("profile_photo")),
                    "Profile updated successfully."
            );

            return ResponseEntity.ok(response);

            } catch (HttpClientErrorException e) {
                e.printStackTrace();
                System.out.println("Supabase error body: " + e.getResponseBodyAsString());
                return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());

            } catch (RestClientResponseException e) {
                e.printStackTrace();
                return ResponseEntity.status(400).body(e.getResponseBodyAsString());

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body(e.getMessage());
            }
    }

    private String value(Object raw) {
        return raw == null ? null : raw.toString();
    }
}
