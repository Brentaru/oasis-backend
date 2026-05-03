package com.oasis.backend.reader.getchapternavigation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.backend.reader.getchapternavigation.dto.GetChapterNavigationResponse;
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
public class GetChapterNavigationService {

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

    public ResponseEntity<?> getChapterNavigation(String seriesId, String chapterId) {

        if (seriesId == null || seriesId.isBlank()) {
            return ResponseEntity.badRequest().body("Series ID is required.");
        }
        if (chapterId == null || chapterId.isBlank()) {
            return ResponseEntity.badRequest().body("Chapter ID is required.");
        }

        try {
            RestTemplate rt = new RestTemplate();

            Integer currentChapterNumber = findCurrentChapterNumber(rt, seriesId, chapterId);
            if (currentChapterNumber == null) {
                return ResponseEntity.status(404).body("Chapter not found.");
            }

            ChapterRef previousChapter = findPreviousChapter(rt, seriesId, currentChapterNumber);
            ChapterRef nextChapter = findNextChapter(rt, seriesId, currentChapterNumber);

            GetChapterNavigationResponse response = new GetChapterNavigationResponse(
                    previousChapter.chapterId(),
                    previousChapter.chapterNumber(),
                    nextChapter.chapterId(),
                    nextChapter.chapterNumber()
            );

            return ResponseEntity.ok(response);

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Failed to get chapter navigation.");

        } catch (RestClientResponseException e) {
            return ResponseEntity.status(400).body("Failed to get chapter navigation.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error. Please try again.");
        }
    }

    private Integer findCurrentChapterNumber(RestTemplate rt, String seriesId, String chapterId) throws Exception {
        String url = supabaseUrl + "/rest/v1/" + chaptersTable
                + "?id=eq." + encode(chapterId)
                + "&series_id=eq." + encode(seriesId)
                + "&select=chapter_number"
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

        return integerValue(rows.get(0).get("chapter_number"));
    }

    private ChapterRef findPreviousChapter(RestTemplate rt, String seriesId, Integer currentChapterNumber) throws Exception {
        String url = supabaseUrl + "/rest/v1/" + chaptersTable
                + "?series_id=eq." + encode(seriesId)
                + "&chapter_number=lt." + currentChapterNumber
                + "&select=id,chapter_number"
                + "&order=chapter_number.desc"
                + "&limit=1";

        return findChapterRef(rt, url);
    }

    private ChapterRef findNextChapter(RestTemplate rt, String seriesId, Integer currentChapterNumber) throws Exception {
        String url = supabaseUrl + "/rest/v1/" + chaptersTable
                + "?series_id=eq." + encode(seriesId)
                + "&chapter_number=gt." + currentChapterNumber
                + "&select=id,chapter_number"
                + "&order=chapter_number.asc"
                + "&limit=1";

        return findChapterRef(rt, url);
    }

    private ChapterRef findChapterRef(RestTemplate rt, String url) throws Exception {
        HttpEntity<Void> entity = new HttpEntity<>(supabaseHeaders());
        ResponseEntity<String> res = rt.exchange(url, HttpMethod.GET, entity, String.class);

        List<Map<String, Object>> rows = objectMapper.readValue(
                res.getBody(),
                new TypeReference<List<Map<String, Object>>>() {
                }
        );

        if (rows.isEmpty()) {
            return new ChapterRef(null, null);
        }

        Map<String, Object> row = rows.get(0);
        return new ChapterRef(
                value(row.get("id")),
                integerValue(row.get("chapter_number"))
        );
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

    private record ChapterRef(String chapterId, Integer chapterNumber) {
    }
}
