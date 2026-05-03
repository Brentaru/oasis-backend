package com.oasis.backend.reader.saveprogress.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.backend.reader.saveprogress.dto.SaveProgressRequest;
import com.oasis.backend.reader.saveprogress.dto.SaveProgressResponse;
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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SaveProgressService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anonKey}")
    private String anonKey;

    @Value("${supabase.serviceRoleKey:}")
    private String serviceRoleKey;

    @Value("${supabase.readingProgressTable:reading_progress}")
    private String readingProgressTable;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String writeKey() {
        if (serviceRoleKey != null && !serviceRoleKey.isBlank()) {
            return serviceRoleKey;
        }
        return anonKey;
    }

    private HttpHeaders supabaseHeaders() {
        String key = writeKey();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", key);
        headers.set("Authorization", "Bearer " + key);
        headers.set("Prefer", "resolution=merge-duplicates,return=representation");
        return headers;
    }

    public ResponseEntity<?> saveProgress(String userId, String seriesId, SaveProgressRequest req) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body("User ID is required.");
        }
        if (!isValidUuid(userId)) {
            return ResponseEntity.badRequest().body("Invalid user ID format.");
        }
        if (seriesId == null || seriesId.isBlank()) {
            return ResponseEntity.badRequest().body("Series ID is required.");
        }
        if (!isValidUuid(seriesId)) {
            return ResponseEntity.badRequest().body("Invalid series ID format.");
        }
        if (req == null) {
            return ResponseEntity.badRequest().body("Request body is required.");
        }
        if (req.chapterId() == null || req.chapterId().isBlank()) {
            return ResponseEntity.badRequest().body("Chapter ID is required.");
        }
        if (!isValidUuid(req.chapterId())) {
            return ResponseEntity.badRequest().body("Invalid chapter ID format.");
        }
        if (req.chapterNumber() == null) {
            return ResponseEntity.badRequest().body("Chapter number is required.");
        }
        if (req.lastReadPage() == null) {
            return ResponseEntity.badRequest().body("Last read page is required.");
        }

        String url = supabaseUrl + "/rest/v1/" + readingProgressTable + "?on_conflict=user_id,series_id";

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("series_id", seriesId);
        body.put("chapter_id", req.chapterId());
        body.put("chapter_number", req.chapterNumber());
        body.put("last_read_page", req.lastReadPage());
        body.put("updated_at", Instant.now().toString());

        try {
            RestTemplate rt = new RestTemplate();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, supabaseHeaders());
            ResponseEntity<String> res = rt.exchange(url, HttpMethod.POST, entity, String.class);

            List<Map<String, Object>> rows = objectMapper.readValue(
                    res.getBody(),
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );

            if (rows.isEmpty()) {
                return ResponseEntity.ok("Progress saved successfully.");
            }

            Map<String, Object> row = rows.get(0);
            SaveProgressResponse response = new SaveProgressResponse(
                    value(row.get("user_id")),
                    value(row.get("series_id")),
                    value(row.get("chapter_id")),
                    integerValue(row.get("chapter_number")),
                    integerValue(row.get("last_read_page")),
                    "Progress saved successfully."
            );

            return ResponseEntity.ok(response);

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Failed to save progress.");

        } catch (RestClientResponseException e) {
            return ResponseEntity.status(400).body("Failed to save progress.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error. Please try again.");
        }
    }

    private String value(Object raw) {
        return raw == null ? null : raw.toString();
    }

    private Integer integerValue(Object raw) {
        if (raw == null) {
            return null;
        }
        try {
            return Integer.parseInt(raw.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isValidUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
