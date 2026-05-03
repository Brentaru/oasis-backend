package com.oasis.backend.library.getseriesdetails.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.backend.library.getseriesdetails.dto.GetSeriesDetailsResponse;
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
public class GetSeriesDetailsService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anonKey}")
    private String anonKey;

    @Value("${supabase.serviceRoleKey:}")
    private String serviceRoleKey;

    @Value("${supabase.seriesTable:series}")
    private String seriesTable;

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

    public ResponseEntity<?> getSeriesDetails(String seriesId) {

        if (seriesId == null || seriesId.isBlank()) {
            return ResponseEntity.badRequest().body("Series ID is required.");
        }

        String url = supabaseUrl + "/rest/v1/" + seriesTable
                + "?id=eq." + encode(seriesId)
                + "&select=id,title,author,cover_image,description,genre,status,rating";

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
                return ResponseEntity.status(404).body("Series not found.");
            }

            Map<String, Object> row = rows.get(0);
            ChapterStats chapterStats = loadChapterStats(rt, seriesId);

            GetSeriesDetailsResponse response = new GetSeriesDetailsResponse(
                    value(row.get("id")),
                    value(row.get("title")),
                    value(row.get("author")),
                    value(row.get("cover_image")),
                    value(row.get("description")),
                    value(row.get("genre")),
                    value(row.get("status")),
                    number(row.get("rating")),
                    chapterStats.totalChapters(),
                    chapterStats.latestChapterNumber()
            );

            return ResponseEntity.ok(response);

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Failed to get series details.");

        } catch (RestClientResponseException e) {
            return ResponseEntity.status(400).body("Failed to get series details.");

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

    private ChapterStats loadChapterStats(RestTemplate rt, String seriesId) throws Exception {
        String url = supabaseUrl + "/rest/v1/" + chaptersTable
                + "?series_id=eq." + encode(seriesId)
                + "&select=chapter_number";

        HttpEntity<Void> entity = new HttpEntity<>(supabaseHeaders());
        ResponseEntity<String> res = rt.exchange(url, HttpMethod.GET, entity, String.class);

        List<Map<String, Object>> rows = objectMapper.readValue(
                res.getBody(),
                new TypeReference<List<Map<String, Object>>>() {
                }
        );

        Integer latestChapterNumber = null;
        for (Map<String, Object> row : rows) {
            Integer chapterNumber = integerValue(row.get("chapter_number"));
            if (chapterNumber == null) {
                continue;
            }

            if (latestChapterNumber == null || chapterNumber > latestChapterNumber) {
                latestChapterNumber = chapterNumber;
            }
        }

        return new ChapterStats(rows.size(), latestChapterNumber);
    }

    private record ChapterStats(Integer totalChapters, Integer latestChapterNumber) {
    }
}

