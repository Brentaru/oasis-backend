package com.oasis.backend.profile.deleteaccount.service;

import com.oasis.backend.profile.deleteaccount.dto.DeleteAccountResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class DeleteAccountService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.serviceRoleKey:}")
    private String serviceRoleKey;

    @Value("${supabase.profileTable:profiles}")
    private String profileTable;

    @Value("${supabase.savedTitlesTable:saved_titles}")
    private String savedTitlesTable;

    @Value("${supabase.readingHistoryTable:reading_history}")
    private String readingHistoryTable;

    public ResponseEntity<?> deleteAccount(String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body("User ID is required.");
        }

        if (serviceRoleKey == null || serviceRoleKey.isBlank()) {
            return ResponseEntity.status(500).body("Supabase service role key is required.");
        }

        try {
            RestTemplate rt = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

            deleteRows(rt, savedTitlesTable, "user_id", userId);
            deleteRows(rt, readingHistoryTable, "user_id", userId);
            deleteRows(rt, profileTable, "user_id", userId);
            deleteAuthUser(rt, userId);

            return ResponseEntity.ok(new DeleteAccountResponse("Account deleted successfully."));

        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(404).body("Account not found.");

        } catch (RestClientResponseException e) {
            return ResponseEntity.status(400).body("Failed to delete account.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error. Please try again.");
        }
    }

    private void deleteRows(RestTemplate rt, String table, String column, String value) {
        String url = supabaseUrl + "/rest/v1/" + table
                + "?" + column + "=eq." + encode(value);

        rt.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers()), String.class);
    }

    private void deleteAuthUser(RestTemplate rt, String userId) {
        String url = supabaseUrl + "/auth/v1/admin/users/" + encode(userId);
        rt.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers()), String.class);
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", serviceRoleKey);
        headers.set("Authorization", "Bearer " + serviceRoleKey);
        return headers;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
