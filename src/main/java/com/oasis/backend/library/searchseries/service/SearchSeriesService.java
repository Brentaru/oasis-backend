package com.oasis.backend.library.searchseries.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.backend.library.searchseries.dto.SearchSeriesResponse;
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
public class SearchSeriesService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anonKey}")
    private String anonKey;

    @Value("${supabase.serviceRoleKey:}")
    private String serviceRoleKey;

    @Value("${supabase.seriesTable:series}")
    private String seriesTable;

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

    public ResponseEntity<?> searchSeries(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return ResponseEntity.badRequest().body("Keyword is required.");
        }

        String cleanedKeyword = keyword.trim();
        String likeValue = encode("*" + cleanedKeyword + "*");

        String url = supabaseUrl + "/rest/v1/" + seriesTable
                + "?select=id,title,author,cover_image,genre,status,rating"
                + "&or=(title.ilike." + likeValue
                + ",author.ilike." + likeValue
                + ",genre.ilike." + likeValue
                + ",description.ilike." + likeValue + ")"
                + "&order=title.asc";

        try {
            RestTemplate rt = new RestTemplate();
            HttpEntity<Void> entity = new HttpEntity<>(supabaseHeaders());
            ResponseEntity<String> res = rt.exchange(url, HttpMethod.GET, entity, String.class);

            List<Map<String, Object>> rows = objectMapper.readValue(
                    res.getBody(),
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );

            List<SearchSeriesResponse> response = rows.stream()
                    .map(row -> new SearchSeriesResponse(
                            value(row.get("id")),
                            value(row.get("title")),
                            value(row.get("author")),
                            value(row.get("cover_image")),
                            value(row.get("genre")),
                            value(row.get("status")),
                            number(row.get("rating"))
                    ))
                    .toList();

            return ResponseEntity.ok(response);

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Failed to search series.");

        } catch (RestClientResponseException e) {
            return ResponseEntity.status(400).body("Failed to search series.");

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

    private Double number(Object raw) {
        if (raw == null) {
            return null;
        }
        try {
            return Double.parseDouble(raw.toString());
        } catch (Exception e) {
            return null;
        }
    }
}

