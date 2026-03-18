package com.oasis.backend.profile.uploadphoto.service;

import com.oasis.backend.profile.uploadphoto.dto.UploadPhotoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UploadPhotoService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anonKey}")
    private String anonKey;

    @Value("${supabase.serviceRoleKey:}")
    private String serviceRoleKey;

    @Value("${supabase.profileTable:profiles}")
    private String profileTable;

    private HttpHeaders jsonHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", token);
        headers.set("Authorization", "Bearer " + token);
        headers.set("Prefer", "return=representation");
        return headers;
    }

    public ResponseEntity<?> uploadPhoto(String userId, MultipartFile file) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body("User ID is required.");
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Photo file is required.");
        }

        String contentType = file.getContentType();
        String originalFileName = file.getOriginalFilename();

        if (contentType == null || contentType.isBlank()) {
            return ResponseEntity.badRequest().body("File type is required.");
        }

        if (!isAllowedImage(contentType, originalFileName)) {
            return ResponseEntity.badRequest().body("Only JPG and PNG files are allowed.");
        }

        if (serviceRoleKey == null || serviceRoleKey.isBlank()) {
            return ResponseEntity.status(500).body("Supabase service role key is required.");
        }

        String token = serviceRoleKey;

        try {
            byte[] photoBytes = file.getBytes();

            String extension = resolveExtension(contentType, originalFileName);
            String fileName = userId + "_" + System.currentTimeMillis() + extension;

            // Supabase/Postgres bytea accepts hex format like: \xFFD8...
            String byteaHex = toPostgresBytea(photoBytes);

            String url = supabaseUrl + "/rest/v1/" + profileTable + "?user_id=eq." + userId;

            Map<String, Object> body = new HashMap<>();
            body.put("photo_bytes", byteaHex);
            body.put("profile_photo", fileName);

            System.out.println("Upload photo DB request -> url: " + url);
            System.out.println("Upload photo DB request -> contentType: " + contentType + ", size: " + file.getSize());

            RestTemplate rt = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, jsonHeaders(token));
            ResponseEntity<List> res = rt.exchange(url, HttpMethod.PATCH, entity, List.class);

            List rows = res.getBody();
            if (rows == null || rows.isEmpty()) {
                return ResponseEntity.status(404).body("Profile not found.");
            }

            return ResponseEntity.ok(new UploadPhotoResponse("Photo uploaded successfully.", fileName));

        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            System.out.println("Supabase DB upload error body: " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body("Failed to upload photo. " + e.getResponseBodyAsString());

        } catch (RestClientResponseException e) {
            e.printStackTrace();
            System.out.println("Supabase upload error status: " + e.getRawStatusCode());
            System.out.println("Supabase upload error body: " + e.getResponseBodyAsString());
            return ResponseEntity.status(400).body("Failed to upload photo. " + e.getResponseBodyAsString());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Server error. " + e.getMessage());
        }
    }

    private boolean isAllowedImage(String contentType, String originalFileName) {
        if (contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/jpg")) {
            return true;
        }
        if (originalFileName == null) {
            return false;
        }
        String lower = originalFileName.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
    }

    private String resolveExtension(String contentType, String originalFileName) {
        if (contentType.equals("image/png")) {
            return ".png";
        }
        if (originalFileName != null && originalFileName.toLowerCase().endsWith(".jpeg")) {
            return ".jpeg";
        }
        return ".jpg";
    }

    private String toPostgresBytea(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return "\\x" + hex;
    }
}
