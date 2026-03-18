package com.oasis.backend.profile.getprofile.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.backend.profile.getprofile.dto.GetProfileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GetProfileService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anonKey}")
    private String anonKey;

    @Value("${supabase.profileTable:profiles}")
    private String profileTable;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpHeaders supabaseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", anonKey);
        headers.set("Authorization", "Bearer " + anonKey);
        return headers;
    }

    public ResponseEntity<?> getProfile(String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body("User ID is required.");
        }

        String url = supabaseUrl + "/rest/v1/" + profileTable
                + "?user_id=eq." + userId
                + "&select=user_id,full_name,email,phone,bio,profile_photo";

        try {
            RestTemplate rt = new RestTemplate();
            HttpEntity<Void> entity = new HttpEntity<>(supabaseHeaders());
            ResponseEntity<String> res = rt.exchange(url, HttpMethod.GET, entity, String.class);

            List<Map<String, Object>> rows = objectMapper.readValue(
                    res.getBody(),
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );

            if (rows.isEmpty()) {
                return ResponseEntity.status(404).body("Profile not found.");
            }

            Map<String, Object> row = rows.get(0);

            GetProfileResponse response = new GetProfileResponse(
                    value(row.get("user_id")),
                    value(row.get("full_name")),
                    value(row.get("email")),
                    value(row.get("phone")),
                    value(row.get("bio")),
                    value(row.get("profile_photo"))
            );

            return ResponseEntity.ok(response);

        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(404).body("Profile not found.");

        } catch (RestClientResponseException e) {
            return ResponseEntity.status(400).body("Failed to get profile.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error. Please try again.");
        }
    }

    private String value(Object raw) {
        return raw == null ? null : raw.toString();
    }
}
