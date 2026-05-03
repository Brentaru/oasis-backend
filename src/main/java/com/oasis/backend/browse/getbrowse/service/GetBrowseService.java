package com.oasis.backend.browse.getbrowse.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.backend.browse.getbrowse.dto.GetBrowseResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GetBrowseService {

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

    public ResponseEntity<?> getBrowse() {

        String url = supabaseUrl + "/rest/v1/" + seriesTable
                + "?select=id,title,author,cover_image,description,genre,status,rating"
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

            List<GetBrowseResponse> response = rows.stream()
                    .map(row -> new GetBrowseResponse(
                            value(row.get("id")),
                            value(row.get("title")),
                            value(row.get("author")),
                            value(row.get("cover_image")),
                            value(row.get("description")),
                            value(row.get("genre")),
                            value(row.get("status")),
                            number(row.get("rating"))
                    ))
                    .toList();

            return ResponseEntity.ok(response);

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Failed to get browse data.");

        } catch (RestClientResponseException e) {
            return ResponseEntity.status(400).body("Failed to get browse data.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error. Please try again.");
        }
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
