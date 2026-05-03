package com.oasis.backend.accountlibrary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class AccountLibraryService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anonKey}")
    private String anonKey;

    @Value("${supabase.serviceRoleKey:}")
    private String serviceRoleKey;

    @Value("${supabase.savedTitlesTable:saved_titles}")
    private String savedTitlesTable;

    @Value("${supabase.readingHistoryTable:reading_history}")
    private String readingHistoryTable;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<?> getSavedTitles(String userId) {
        if (!isValidUuid(userId)) {
            return ResponseEntity.badRequest().body("Invalid user ID.");
        }

        String url = supabaseUrl + "/rest/v1/" + savedTitlesTable
                + "?user_id=eq." + encode(userId)
                + "&select=*"
                + "&order=saved_at.desc";

        return getList(url, "Failed to load saved titles.");
    }

    public ResponseEntity<?> saveTitle(String userId, Map<String, Object> request) {
        if (!isValidUuid(userId)) {
            return ResponseEntity.badRequest().body("Invalid user ID.");
        }
        if (isBlank(value(request.get("seriesId")))) {
            return ResponseEntity.badRequest().body("Series ID is required.");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("user_id", userId);
        body.put("source", valueOr(request.get("source"), "mangadex"));
        body.put("source_id", valueOr(request.get("sourceId"), stripMangaDexPrefix(value(request.get("seriesId")))));
        body.put("series_id", value(request.get("seriesId")));
        body.put("title", valueOr(request.get("title"), "Untitled"));
        body.put("author", nullable(request.get("author")));
        body.put("artist", nullable(request.get("artist")));
        body.put("cover_image", nullable(request.get("coverImage")));
        body.put("description", nullable(request.get("description")));
        body.put("genre", nullable(request.get("genre")));
        body.put("genres", listValue(request.get("genres")));
        body.put("status", nullable(request.get("status")));
        body.put("content_rating", nullable(request.get("contentRating")));
        body.put("year", integerValue(request.get("year")));
        body.put("total_chapters", integerValue(request.get("totalChapters")));
        body.put("latest_chapter_number", integerValue(request.get("latestChapterNumber")));
        body.put("updated_at", OffsetDateTime.now().toString());

        String url = supabaseUrl + "/rest/v1/" + savedTitlesTable
                + "?on_conflict=user_id,series_id";

        return upsert(url, body, "Saved title.");
    }

    public ResponseEntity<?> removeSavedTitle(String userId, String seriesId) {
        if (!isValidUuid(userId)) {
            return ResponseEntity.badRequest().body("Invalid user ID.");
        }

        String url = supabaseUrl + "/rest/v1/" + savedTitlesTable
                + "?user_id=eq." + encode(userId)
                + "&series_id=eq." + encode(seriesId);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers()), String.class);
            return ResponseEntity.ok(Map.of("message", "Saved title removed."));
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Failed to remove saved title.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to remove saved title.");
        }
    }

    public ResponseEntity<?> getReadingHistory(String userId) {
        if (!isValidUuid(userId)) {
            return ResponseEntity.badRequest().body("Invalid user ID.");
        }

        String url = supabaseUrl + "/rest/v1/" + readingHistoryTable
                + "?user_id=eq." + encode(userId)
                + "&select=*"
                + "&order=updated_at.desc"
                + "&limit=50";

        return getList(url, "Failed to load reading history.");
    }

    public ResponseEntity<?> saveReadingHistory(String userId, Map<String, Object> request) {
        if (!isValidUuid(userId)) {
            return ResponseEntity.badRequest().body("Invalid user ID.");
        }
        if (isBlank(value(request.get("seriesId"))) || isBlank(value(request.get("chapterId")))) {
            return ResponseEntity.badRequest().body("Series and chapter are required.");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("user_id", userId);
        body.put("source", valueOr(request.get("source"), "mangadex"));
        body.put("source_id", valueOr(request.get("sourceId"), stripMangaDexPrefix(value(request.get("seriesId")))));
        body.put("series_id", value(request.get("seriesId")));
        body.put("chapter_id", value(request.get("chapterId")));
        body.put("chapter_number", integerValue(request.get("chapterNumber")));
        body.put("chapter_title", nullable(request.get("chapterTitle")));
        body.put("last_read_page", integerValue(request.get("lastReadPage")) == null ? 1 : integerValue(request.get("lastReadPage")));
        body.put("title", valueOr(request.get("title"), "Untitled"));
        body.put("cover_image", nullable(request.get("coverImage")));
        body.put("genre", nullable(request.get("genre")));
        body.put("status", nullable(request.get("status")));
        body.put("updated_at", OffsetDateTime.now().toString());

        String url = supabaseUrl + "/rest/v1/" + readingHistoryTable
                + "?on_conflict=user_id,series_id,chapter_id";

        return upsert(url, body, "Reading history saved.");
    }

    private ResponseEntity<?> getList(String url, String errorMessage) {
        try {
            ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers()), String.class);
            List<Map<String, Object>> rows = objectMapper.readValue(
                    res.getBody(),
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );
            return ResponseEntity.ok(rows.stream().map(this::toClient).toList());
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(errorMessage);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(errorMessage);
        }
    }

    private ResponseEntity<?> upsert(String url, Map<String, Object> body, String message) {
        try {
            HttpHeaders headers = headers();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Prefer", "resolution=merge-duplicates,return=representation");

            ResponseEntity<String> res = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(List.of(body), headers),
                    String.class
            );

            List<Map<String, Object>> rows = objectMapper.readValue(
                    res.getBody(),
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );
            return ResponseEntity.ok(rows.isEmpty() ? Map.of("message", message) : toClient(rows.get(0)));
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString().isBlank() ? message : e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(message);
        }
    }

    private Map<String, Object> toClient(Map<String, Object> row) {
        Map<String, Object> item = new LinkedHashMap<>();
        put(item, "id", row.get("id"));
        put(item, "userId", row.get("user_id"));
        put(item, "source", row.get("source"));
        put(item, "sourceId", row.get("source_id"));
        put(item, "seriesId", row.get("series_id"));
        put(item, "chapterId", row.get("chapter_id"));
        put(item, "chapterNumber", row.get("chapter_number"));
        put(item, "chapterTitle", row.get("chapter_title"));
        put(item, "lastReadPage", row.get("last_read_page"));
        put(item, "title", row.get("title"));
        put(item, "author", row.get("author"));
        put(item, "artist", row.get("artist"));
        put(item, "coverImage", row.get("cover_image"));
        put(item, "description", row.get("description"));
        put(item, "genre", row.get("genre"));
        put(item, "genres", row.get("genres"));
        put(item, "status", row.get("status"));
        put(item, "contentRating", row.get("content_rating"));
        put(item, "year", row.get("year"));
        put(item, "totalChapters", row.get("total_chapters"));
        put(item, "latestChapterNumber", row.get("latest_chapter_number"));
        put(item, "savedAt", row.get("saved_at"));
        put(item, "updatedAt", row.get("updated_at"));
        return item;
    }

    private void put(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    private HttpHeaders headers() {
        String key = serviceRoleKey != null && !serviceRoleKey.isBlank() ? serviceRoleKey : anonKey;
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", key);
        headers.set("Authorization", "Bearer " + key);
        return headers;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private Object nullable(Object raw) {
        String value = value(raw);
        return isBlank(value) ? null : value;
    }

    private String value(Object raw) {
        return raw == null ? "" : raw.toString();
    }

    private String valueOr(Object raw, String fallback) {
        String value = value(raw);
        return isBlank(value) ? fallback : value;
    }

    private List<String> listValue(Object raw) {
        if (raw instanceof List<?> list) {
            return list.stream().map(Object::toString).filter(v -> !isBlank(v)).toList();
        }
        String value = value(raw);
        if (isBlank(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(",")).map(String::trim).filter(v -> !isBlank(v)).toList();
    }

    private Integer integerValue(Object raw) {
        if (raw == null || isBlank(raw.toString())) {
            return null;
        }
        try {
            return Integer.parseInt(raw.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isValidUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String stripMangaDexPrefix(String seriesId) {
        return seriesId == null ? "" : seriesId.replaceFirst("^mangadex:", "");
    }
}
