package com.oasis.backend.source.mangadex;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.backend.source.mangadex.dto.MangaDexChapterNavigationResponse;
import com.oasis.backend.source.mangadex.dto.MangaDexChapterResponse;
import com.oasis.backend.source.mangadex.dto.MangaDexPageResponse;
import com.oasis.backend.source.mangadex.dto.MangaDexSeriesDetailsResponse;
import com.oasis.backend.source.mangadex.dto.MangaDexSeriesResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MangaDexService {

    private static final String API_BASE_URL = "https://api.mangadex.org";
    private static final String COVER_BASE_URL = "https://uploads.mangadex.org/covers";
    private static final String SOURCE = "mangadex";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<?> getSeries(String query, Integer limit, String status, String contentRating, String order) {
        int safeLimit = clampLimit(limit);
        String safeContentRating = isBlank(contentRating) ? "safe,suggestive" : contentRating;
        String safeOrder = isBlank(order) ? "followedCount" : order;

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(API_BASE_URL + "/manga")
                .queryParam("limit", safeLimit)
                .queryParam("offset", 0)
                .queryParam("includes[]", "cover_art")
                .queryParam("includes[]", "author")
                .queryParam("includes[]", "artist")
                .queryParam("availableTranslatedLanguage[]", "en")
                .queryParam("hasAvailableChapters", true)
                .queryParam("order[" + orderKey(safeOrder) + "]", "desc");

        for (String rating : safeContentRating.split(",")) {
            if (!isBlank(rating)) {
                builder.queryParam("contentRating[]", rating.trim());
            }
        }

        if (!isBlank(status) && !"all".equalsIgnoreCase(status)) {
            builder.queryParam("status[]", status.trim());
        }

        if (query != null && !query.isBlank()) {
            builder.queryParam("title", query.trim());
        } else {
            builder.queryParam("hasAvailableChapters", true);
        }

        try {
            Map<String, Object> body = get(builder.build().encode().toUri());
            List<Map<String, Object>> data = list(body.get("data"));

            List<MangaDexSeriesResponse> response = data.stream()
                    .map(this::toSeries)
                    .toList();

            return ResponseEntity.ok(response);
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Failed to get MangaDex series.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("MangaDex is unavailable. Please try again.");
        }
    }

    public ResponseEntity<?> getSeriesDetails(String mangaId) {
        if (isBlank(mangaId)) {
            return ResponseEntity.badRequest().body("Manga ID is required.");
        }

        URI uri = UriComponentsBuilder
                .fromHttpUrl(API_BASE_URL + "/manga/" + mangaId)
                .queryParam("includes[]", "cover_art")
                .queryParam("includes[]", "author")
                .queryParam("includes[]", "artist")
                .build()
                .encode()
                .toUri();

        try {
            Map<String, Object> body = get(uri);
            Map<String, Object> data = map(body.get("data"));
            ChapterStats stats = loadChapterStats(mangaId);

            return ResponseEntity.ok(toSeriesDetails(data, stats));
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Failed to get MangaDex series details.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("MangaDex is unavailable. Please try again.");
        }
    }

    public ResponseEntity<?> getChapters(String mangaId) {
        if (isBlank(mangaId)) {
            return ResponseEntity.badRequest().body("Manga ID is required.");
        }

        try {
            return ResponseEntity.ok(loadChapters(mangaId, 100));
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Failed to get MangaDex chapters.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("MangaDex is unavailable. Please try again.");
        }
    }

    public ResponseEntity<?> getChapterPages(String chapterId) {
        if (isBlank(chapterId)) {
            return ResponseEntity.badRequest().body("Chapter ID is required.");
        }

        URI uri = UriComponentsBuilder
                .fromHttpUrl(API_BASE_URL + "/at-home/server/" + chapterId)
                .build()
                .encode()
                .toUri();

        try {
            Map<String, Object> body = get(uri);
            String baseUrl = value(body.get("baseUrl"));
            Map<String, Object> chapter = map(body.get("chapter"));
            String hash = value(chapter.get("hash"));
            List<Object> files = rawList(chapter.get("dataSaver"));

            if (files.isEmpty()) {
                files = rawList(chapter.get("data"));
            }

            String quality = rawList(chapter.get("dataSaver")).isEmpty() ? "data" : "data-saver";
            List<MangaDexPageResponse> pages = new ArrayList<>();
            for (int i = 0; i < files.size(); i++) {
                String file = value(files.get(i));
                pages.add(new MangaDexPageResponse(
                        chapterId + "-" + (i + 1),
                        i + 1,
                        baseUrl + "/" + quality + "/" + hash + "/" + file
                ));
            }

            return ResponseEntity.ok(pages);
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Failed to get MangaDex chapter pages.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("MangaDex is unavailable. Please try again.");
        }
    }

    public ResponseEntity<?> getChapterNavigation(String mangaId, String chapterId) {
        if (isBlank(mangaId)) {
            return ResponseEntity.badRequest().body("Manga ID is required.");
        }
        if (isBlank(chapterId)) {
            return ResponseEntity.badRequest().body("Chapter ID is required.");
        }

        try {
            List<MangaDexChapterResponse> chapters = loadChapters(mangaId, 100);
            chapters.sort(Comparator.comparing(
                    MangaDexChapterResponse::chapterNumber,
                    Comparator.nullsLast(Integer::compareTo)
            ));

            for (int i = 0; i < chapters.size(); i++) {
                MangaDexChapterResponse current = chapters.get(i);
                if (!chapterId.equals(current.chapterId())) {
                    continue;
                }

                MangaDexChapterResponse previous = i > 0 ? chapters.get(i - 1) : null;
                MangaDexChapterResponse next = i < chapters.size() - 1 ? chapters.get(i + 1) : null;

                return ResponseEntity.ok(new MangaDexChapterNavigationResponse(
                        previous == null ? null : previous.chapterId(),
                        previous == null ? null : previous.chapterNumber(),
                        next == null ? null : next.chapterId(),
                        next == null ? null : next.chapterNumber()
                ));
            }

            return ResponseEntity.ok(new MangaDexChapterNavigationResponse(null, null, null, null));
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Failed to get MangaDex chapter navigation.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("MangaDex is unavailable. Please try again.");
        }
    }

    public ResponseEntity<?> proxyImage(String imageUrl) {
        if (isBlank(imageUrl)) {
            return ResponseEntity.badRequest().body("Image URL is required.");
        }

        try {
            URI uri = URI.create(imageUrl);
            if (!isAllowedImageHost(uri)) {
                return ResponseEntity.badRequest().body("Unsupported image host.");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
            headers.set("Referer", "https://mangadex.org/");
            headers.set("User-Agent", "Mozilla/5.0 OasisProject/1.0");

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    byte[].class
            );

            HttpHeaders outputHeaders = new HttpHeaders();
            MediaType contentType = response.getHeaders().getContentType();
            outputHeaders.setContentType(contentType == null ? MediaType.IMAGE_JPEG : contentType);
            outputHeaders.setCacheControl("public, max-age=86400");

            return ResponseEntity
                    .status(response.getStatusCode())
                    .headers(outputHeaders)
                    .body(response.getBody());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid image URL.");
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Failed to load MangaDex image.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to load MangaDex image.");
        }
    }

    private List<MangaDexChapterResponse> loadChapters(String mangaId, int limit) throws Exception {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(API_BASE_URL + "/manga/" + mangaId + "/feed")
                .queryParam("limit", limit)
                .queryParam("offset", 0)
                .queryParam("translatedLanguage[]", "en")
                .queryParam("contentRating[]", "safe")
                .queryParam("contentRating[]", "suggestive")
                .queryParam("order[chapter]", "desc")
                .build()
                .encode()
                .toUri();

        Map<String, Object> body = get(uri);
        List<Map<String, Object>> data = list(body.get("data"));
        Map<String, MangaDexChapterResponse> uniqueByChapterNumber = new LinkedHashMap<>();

        for (Map<String, Object> item : data) {
            Map<String, Object> attributes = map(item.get("attributes"));
            Integer pageCount = integerValue(attributes.get("pages"));
            String externalUrl = value(attributes.get("externalUrl"));
            Integer chapterNumber = integerValue(attributes.get("chapter"));
            if (chapterNumber == null || pageCount == null || pageCount <= 0 || !isBlank(externalUrl)) {
                continue;
            }

            uniqueByChapterNumber.putIfAbsent(
                    chapterNumber.toString(),
                    new MangaDexChapterResponse(
                            value(item.get("id")),
                            chapterNumber,
                            value(attributes.get("title"))
                    )
            );
        }

        return new ArrayList<>(uniqueByChapterNumber.values());
    }

    private ChapterStats loadChapterStats(String mangaId) throws Exception {
        List<MangaDexChapterResponse> chapters = loadChapters(mangaId, 100);
        Integer latest = null;

        for (MangaDexChapterResponse chapter : chapters) {
            if (chapter.chapterNumber() == null) {
                continue;
            }

            if (latest == null || chapter.chapterNumber() > latest) {
                latest = chapter.chapterNumber();
            }
        }

        return new ChapterStats(chapters.size(), latest);
    }

    private MangaDexSeriesResponse toSeries(Map<String, Object> data) {
        Map<String, Object> attributes = map(data.get("attributes"));
        String mangaId = value(data.get("id"));

        return new MangaDexSeriesResponse(
                SOURCE + ":" + mangaId,
                SOURCE,
                mangaId,
                localized(attributes.get("title")),
                relationshipName(data, "author"),
                relationshipName(data, "artist"),
                coverUrl(mangaId, data),
                firstTag(attributes),
                tags(attributes),
                value(attributes.get("status")),
                value(attributes.get("contentRating")),
                integerValue(attributes.get("year")),
                null
        );
    }

    private MangaDexSeriesDetailsResponse toSeriesDetails(Map<String, Object> data, ChapterStats stats) {
        Map<String, Object> attributes = map(data.get("attributes"));
        String mangaId = value(data.get("id"));

        return new MangaDexSeriesDetailsResponse(
                SOURCE + ":" + mangaId,
                SOURCE,
                mangaId,
                localized(attributes.get("title")),
                relationshipName(data, "author"),
                relationshipName(data, "artist"),
                coverUrl(mangaId, data),
                localized(attributes.get("description")),
                firstTag(attributes),
                tags(attributes),
                value(attributes.get("status")),
                value(attributes.get("contentRating")),
                integerValue(attributes.get("year")),
                null,
                stats.totalChapters(),
                stats.latestChapterNumber()
        );
    }

    private String coverUrl(String mangaId, Map<String, Object> data) {
        for (Map<String, Object> relationship : list(data.get("relationships"))) {
            if (!"cover_art".equals(value(relationship.get("type")))) {
                continue;
            }

            String fileName = value(map(relationship.get("attributes")).get("fileName"));
            if (!isBlank(fileName)) {
                return COVER_BASE_URL + "/" + mangaId + "/" + fileName + ".256.jpg";
            }
        }

        return null;
    }

    private String firstTag(Map<String, Object> attributes) {
        List<String> tags = tags(attributes);
        return tags.isEmpty() ? null : tags.get(0);
    }

    private List<String> tags(Map<String, Object> attributes) {
        List<String> values = new ArrayList<>();
        for (Map<String, Object> tag : list(attributes.get("tags"))) {
            String name = localized(map(tag.get("attributes")).get("name"));
            if (!isBlank(name)) {
                values.add(name);
            }
        }

        return values;
    }

    private String relationshipName(Map<String, Object> data, String type) {
        for (Map<String, Object> relationship : list(data.get("relationships"))) {
            if (!type.equals(value(relationship.get("type")))) {
                continue;
            }

            String name = value(map(relationship.get("attributes")).get("name"));
            if (!isBlank(name)) {
                return name;
            }
        }

        return null;
    }

    private String orderKey(String order) {
        return switch (order) {
            case "latestUploadedChapter" -> "latestUploadedChapter";
            case "createdAt" -> "createdAt";
            case "updatedAt" -> "updatedAt";
            case "title" -> "title";
            default -> "followedCount";
        };
    }

    private Map<String, Object> get(URI uri) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "OasisProject/1.0");

        ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        return objectMapper.readValue(
                response.getBody(),
                new TypeReference<Map<String, Object>>() {
                }
        );
    }

    private boolean isAllowedImageHost(URI uri) {
        String host = uri.getHost();
        if (host == null) {
            return false;
        }

        return "uploads.mangadex.org".equalsIgnoreCase(host)
                || host.toLowerCase().endsWith(".mangadex.network");
    }

    private String localized(Object raw) {
        Map<String, Object> values = map(raw);
        String english = value(values.get("en"));
        if (!isBlank(english)) {
            return english;
        }

        for (Object value : values.values()) {
            String text = value(value);
            if (!isBlank(text)) {
                return text;
            }
        }

        return null;
    }

    private int clampLimit(Integer limit) {
        if (limit == null) {
            return 24;
        }

        return Math.max(1, Math.min(limit, 50));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String value(Object raw) {
        return raw == null ? null : raw.toString();
    }

    private Integer integerValue(Object raw) {
        if (raw == null) {
            return null;
        }

        try {
            return (int) Double.parseDouble(raw.toString());
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object raw) {
        if (raw instanceof Map<?, ?> rawMap) {
            return (Map<String, Object>) rawMap;
        }

        return Map.of();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Object raw) {
        if (raw instanceof List<?> rawList) {
            return (List<Map<String, Object>>) rawList;
        }

        return List.of();
    }

    private List<Object> rawList(Object raw) {
        if (raw instanceof List<?> rawList) {
            return new ArrayList<>(rawList);
        }

        return List.of();
    }

    private record ChapterStats(Integer totalChapters, Integer latestChapterNumber) {
    }
}
