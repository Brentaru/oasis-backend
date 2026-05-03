package com.oasis.backend.library.getchapters.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.backend.library.getchapters.dto.GetChaptersResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class GetChaptersService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anonKey}")
    private String anonKey;

    @Value("${supabase.serviceRoleKey:}")
    private String serviceRoleKey;

    @Value("${supabase.chaptersTable:chapters}")
    private String chaptersTable;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String readKey() {
        if (serviceRoleKey != null && !serviceRoleKey.isBlank()) {
            return serviceRoleKey;
        }
        return anonKey;
    }

    private HttpHeaders supabaseHeaders() {
        String key = readKey();

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", key);
        headers.set("Authorization", "Bearer " + key);
        return headers;
    }

    public ResponseEntity<?> getChapters(String seriesId) {

        if (seriesId == null || seriesId.isBlank()) {
            return ResponseEntity.badRequest().body("Series ID is required.");
        }

        String url = supabaseUrl + "/rest/v1/" + chaptersTable
                + "?series_id=eq." + encode(seriesId)
                + "&select=id,chapter_number,title"
                + "&order=chapter_number.asc";

        try {
            RestTemplate rt = new RestTemplate();
            HttpEntity<Void> entity = new HttpEntity<>(supabaseHeaders());
            ResponseEntity<String> res = rt.exchange(url, HttpMethod.GET, entity, String.class);

            List<Map<String, Object>> rows = objectMapper.readValue(
                    res.getBody(),
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );

            List<GetChaptersResponse> response = rows.stream()
                    .map(row -> new GetChaptersResponse(
                            value(row.get("id")),
                            integerValue(row.get("chapter_number")),
                            value(row.get("title"))
                    ))
                    .toList();

            return ResponseEntity.ok(response);

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Failed to get chapters.");

        } catch (RestClientResponseException e) {
            return ResponseEntity.status(400).body("Failed to get chapters.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error. Please try again.");
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
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
}

