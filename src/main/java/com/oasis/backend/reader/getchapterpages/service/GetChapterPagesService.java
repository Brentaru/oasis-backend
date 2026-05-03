package com.oasis.backend.reader.getchapterpages.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.backend.reader.getchapterpages.dto.GetChapterPagesResponse;
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
public class GetChapterPagesService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anonKey}")
    private String anonKey;

    @Value("${supabase.serviceRoleKey:}")
    private String serviceRoleKey;

    @Value("${supabase.chaptersTable:chapters}")
    private String chaptersTable;

    @Value("${supabase.chapterPagesTable:chapter_pages}")
    private String chapterPagesTable;

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

    public ResponseEntity<?> getChapterPages(String seriesId, String chapterId) {

        if (seriesId == null || seriesId.isBlank()) {
            return ResponseEntity.badRequest().body("Series ID is required.");
        }
        if (chapterId == null || chapterId.isBlank()) {
            return ResponseEntity.badRequest().body("Chapter ID is required.");
        }

        try {
            RestTemplate rt = new RestTemplate();

            if (!isChapterInSeries(rt, seriesId, chapterId)) {
                return ResponseEntity.status(404).body("Chapter not found.");
            }

            String url = supabaseUrl + "/rest/v1/" + chapterPagesTable
                    + "?chapter_id=eq." + encode(chapterId)
                    + "&select=id,page_number,image_url"
                    + "&order=page_number.asc";

            HttpEntity<Void> entity = new HttpEntity<>(supabaseHeaders());
            ResponseEntity<String> res = rt.exchange(url, HttpMethod.GET, entity, String.class);

            List<Map<String, Object>> rows = objectMapper.readValue(
                    res.getBody(),
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );

            List<GetChapterPagesResponse> response = rows.stream()
                    .map(row -> new GetChapterPagesResponse(
                            value(row.get("id")),
                            integerValue(row.get("page_number")),
                            value(row.get("image_url"))
                    ))
                    .toList();

            return ResponseEntity.ok(response);

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Failed to get chapter pages.");

        } catch (RestClientResponseException e) {
            return ResponseEntity.status(400).body("Failed to get chapter pages.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error. Please try again.");
        }
    }

    private boolean isChapterInSeries(RestTemplate rt, String seriesId, String chapterId) throws Exception {
        String url = supabaseUrl + "/rest/v1/" + chaptersTable
                + "?id=eq." + encode(chapterId)
                + "&series_id=eq." + encode(seriesId)
                + "&select=id";

        HttpEntity<Void> entity = new HttpEntity<>(supabaseHeaders());
        ResponseEntity<String> res = rt.exchange(url, HttpMethod.GET, entity, String.class);

        List<Map<String, Object>> rows = objectMapper.readValue(
                res.getBody(),
                new TypeReference<List<Map<String, Object>>>() {
                }
        );

        return !rows.isEmpty();
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
