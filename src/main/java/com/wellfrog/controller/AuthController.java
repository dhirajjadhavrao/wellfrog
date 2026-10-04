package com.wellfrog.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.wellfrog.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final String googleClientId;

    public AuthController(UserService userService, @Value("${wellfrog.google.client-id:}") String googleClientId) {
        this.userService = userService;
        this.googleClientId = googleClientId;
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> payload) {
        String token = payload.get("idToken");
        if (token == null || token.isBlank()) {
            token = payload.get("accessToken");
        }
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "idToken or accessToken is required"));
        }

        try {
            // Strategy 1: Google Server-side tokeninfo verification (immune to local clock skew)
            Map<String, Object> userData = verifyWithGoogleTokenInfo(token);

            // Strategy 2: Google UserInfo endpoint (if access token provided)
            if (userData == null) {
                userData = verifyWithGoogleUserInfo(token);
            }

            // Strategy 3: Offline ID token verification
            if (userData == null) {
                userData = verifyWithOfflineVerifier(token);
            }

            if (userData != null) {
                String email = (String) userData.get("email");
                String name = (String) userData.get("name");
                String pictureUrl = (String) userData.get("picture");
                String googleId = (String) userData.get("googleId");

                Map<String, Object> authResponse = userService.processUserLogin(email, name, pictureUrl, googleId);
                return ResponseEntity.ok(authResponse);
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid Google ID token"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Google auth verification failed: " + e.getMessage()));
        }
    }

    private Map<String, Object> verifyWithGoogleTokenInfo(String token) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(6))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(response.body());
                if (json.has("email")) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("email", json.get("email").asText());
                    data.put("name", json.has("name") ? json.get("name").asText() : json.get("email").asText().split("@")[0]);
                    data.put("picture", json.has("picture") ? json.get("picture").asText() : "");
                    data.put("googleId", json.has("sub") ? json.get("sub").asText() : "google-" + Math.abs(json.get("email").asText().hashCode()));
                    return data;
                }
            }
        } catch (Exception e) {
            System.err.println("Google tokeninfo check failed: " + e.getMessage());
        }
        return null;
    }

    private Map<String, Object> verifyWithGoogleUserInfo(String token) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(6))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/oauth2/v3/userinfo"))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(response.body());
                if (json.has("email")) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("email", json.get("email").asText());
                    data.put("name", json.has("name") ? json.get("name").asText() : json.get("email").asText().split("@")[0]);
                    data.put("picture", json.has("picture") ? json.get("picture").asText() : "");
                    data.put("googleId", json.has("sub") ? json.get("sub").asText() : "google-" + Math.abs(json.get("email").asText().hashCode()));
                    return data;
                }
            }
        } catch (Exception e) {
            System.err.println("Google userinfo check failed: " + e.getMessage());
        }
        return null;
    }

    private Map<String, Object> verifyWithOfflineVerifier(String token) {
        try {
            GoogleIdTokenVerifier.Builder verifierBuilder = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance()
            );

            if (googleClientId != null && !googleClientId.isBlank()) {
                verifierBuilder.setAudience(Collections.singletonList(googleClientId));
            }

            GoogleIdTokenVerifier verifier = verifierBuilder.build();
            GoogleIdToken idToken = verifier.verify(token);

            if (idToken != null) {
                GoogleIdToken.Payload tokenPayload = idToken.getPayload();
                Map<String, Object> data = new HashMap<>();
                data.put("email", tokenPayload.getEmail());
                data.put("name", (String) tokenPayload.get("name"));
                data.put("picture", (String) tokenPayload.get("picture"));
                data.put("googleId", tokenPayload.getSubject());
                return data;
            }
        } catch (Exception e) {
            System.err.println("Offline verifier failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get public authentication configuration (Google Client ID if configured)
     */
    @GetMapping("/config")
    public ResponseEntity<?> getAuthConfig() {
        return ResponseEntity.ok(Map.of(
                "googleClientId", googleClientId != null ? googleClientId : ""
        ));
    }

    /**
     * Email / Direct Auth for Sign In and Sign Up (creates user if new)
     */
    @PostMapping("/email-auth")
    public ResponseEntity<?> emailAuth(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        String name = (String) payload.get("name");
        Boolean isSignUp = (Boolean) payload.getOrDefault("isSignUp", false);

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        email = email.trim().toLowerCase();

        if (name == null || name.isBlank()) {
            name = email.split("@")[0];
            // Capitalize first letter
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
        }

        String picture = "https://api.dicebear.com/7.x/bottts/svg?seed=" + email;
        String googleId = "user-" + Math.abs(email.hashCode());

        Map<String, Object> authResponse = userService.processUserLogin(email, name.trim(), picture, googleId);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * 1-Click Fast Login for local development & demonstration
     */
    @PostMapping("/dev-login")
    public ResponseEntity<?> devLogin(@RequestBody(required = false) Map<String, String> payload) {
        String email = (payload != null && payload.get("email") != null) ? payload.get("email") : "dhiraj.dev@wellfrog.com";
        String name = (payload != null && payload.get("name") != null) ? payload.get("name") : "Dhiraj Jadhavrao";
        String picture = "https://api.dicebear.com/7.x/bottts/svg?seed=wellfrog";
        String googleId = "dev-google-id-" + Math.abs(email.hashCode());

        Map<String, Object> authResponse = userService.processUserLogin(email, name, picture, googleId);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));
        }
        return ResponseEntity.ok(Map.of(
                "userId", authentication.getPrincipal(),
                "email", authentication.getCredentials()
        ));
    }
}
