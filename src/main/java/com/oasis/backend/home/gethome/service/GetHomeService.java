package com.oasis.backend.home.gethome.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.backend.home.gethome.dto.GetHomeContinueReadingResponse;
import com.oasis.backend.home.gethome.dto.GetHomeResponse;
import com.oasis.backend.home.gethome.dto.GetHomeSeriesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GetHomeService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anonKey}")
    private String anonKey;

    @Value("${supabase.serviceRoleKey:}")
    private String serviceRoleKey;

    @Value("${supabase.seriesTable:series}")
    private String seriesTable;

    @Value("${supabase.readingProgressTable:reading_progress}")
    private String readingProgressTable;

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

    public ResponseEntity<?> getHome(String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body("User ID is required.");
        }
        if (!isValidUuid(userId)) {
            return ResponseEntity.badRequest().body("Invalid user ID format.");
        }

        GetHomeResponse response = new GetHomeResponse(null, List.of(), List.of());
        return ResponseEntity.ok(response);
    }

    private GetHomeSeriesResponse getFeaturedSeries(RestTemplate rt) throws Exception {
        String url = supabaseUrl + "/rest/v1/" + seriesTable
                + "?select=id,title,author,cover_image,description,genre,status,rating"
                + "&order=rating.desc.nullslast"
                + "&limit=1";

        HttpEntity<Void> entity = new HttpEntity<>(supabaseHeaders());
        ResponseEntity<String> res = rt.exchange(url, HttpMethod.GET, entity, String.class);

        List<Map<String, Object>> rows = objectMapper.readValue(
                res.getBody(),
                new TypeReference<List<Map<String, Object>>>() {
                }
        );

        if (rows.isEmpty()) {
            return null;
        }

        return mapSeries(rows.get(0));
    }

    private List<GetHomeSeriesResponse> getRecentSeries(RestTemplate rt) throws Exception {
        String url = supabaseUrl + "/rest/v1/" + seriesTable
                + "?select=id,title,author,cover_image,description,genre,status,rating"
                + "&order=updated_at.desc.nullslast,id.desc"
                + "&limit=10";

        HttpEntity<Void> entity = new HttpEntity<>(supabaseHeaders());
        ResponseEntity<String> res = rt.exchange(url, HttpMethod.GET, entity, String.class);

        List<Map<String, Object>> rows = objectMapper.readValue(
                res.getBody(),
                new TypeReference<List<Map<String, Object>>>() {
                }
        );

        List<GetHomeSeriesResponse> response = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            response.add(mapSeries(row));
        }

        return response;
    }

    private List<GetHomeContinueReadingResponse> getContinueReading(RestTemplate rt, String userId) throws Exception {
        String url = supabaseUrl + "/rest/v1/" + readingProgressTable
                + "?user_id=eq." + encode(userId)
                + "&select=user_id,series_id,chapter_id,chapter_number,last_read_page,updated_at,series:series_id(id,title,author,cover_image,description,genre,status,rating)"
                + "&order=updated_at.desc.nullslast"
                + "&limit=10";

        HttpEntity<Void> entity = new HttpEntity<>(supabaseHeaders());
        ResponseEntity<String> res = rt.exchange(url, HttpMethod.GET, entity, String.class);

        List<Map<String, Object>> rows = objectMapper.readValue(
                res.getBody(),
                new TypeReference<List<Map<String, Object>>>() {
                }
        );

        List<GetHomeContinueReadingResponse> response = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            response.add(mapContinueReading(row));
        }

        return response;
    }

    private GetHomeSeriesResponse mapSeries(Map<String, Object> row) {
        return new GetHomeSeriesResponse(
                value(row.get("id")),
                value(row.get("title")),
                value(row.get("author")),
                value(row.get("cover_image")),
                value(row.get("description")),
                value(row.get("genre")),
                value(row.get("status")),
                number(row.get("rating"))
        );
    }

    private GetHomeContinueReadingResponse mapContinueReading(Map<String, Object> row) {
        Map<?, ?> series = map(row.get("series"));

        return new GetHomeContinueReadingResponse(
                value(row.get("user_id")),
                value(row.get("series_id")),
                value(row.get("chapter_id")),
                integerValue(row.get("chapter_number")),
                integerValue(row.get("last_read_page")),
                value(series.get("title")),
                value(series.get("author")),
                value(series.get("cover_image")),
                value(series.get("description")),
                value(series.get("genre")),
                value(series.get("status")),
                number(series.get("rating"))
        );
    }

    private Map<?, ?> map(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            return map;
        }
        return Map.of();
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
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
