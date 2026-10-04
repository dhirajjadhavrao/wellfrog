package com.wellfrog.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.wellfrog.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
        String idTokenString = payload.get("idToken");
        if (idTokenString == null || idTokenString.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "idToken is required"));
        }

        try {
            GoogleIdTokenVerifier.Builder verifierBuilder = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance()
            );

            if (googleClientId != null && !googleClientId.isBlank()) {
                verifierBuilder.setAudience(Collections.singletonList(googleClientId));
            }

            GoogleIdTokenVerifier verifier = verifierBuilder.build();
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                GoogleIdToken.Payload tokenPayload = idToken.getPayload();
                String email = tokenPayload.getEmail();
                String name = (String) tokenPayload.get("name");
                String pictureUrl = (String) tokenPayload.get("picture");
                String googleId = tokenPayload.getSubject();

                Map<String, Object> authResponse = userService.processUserLogin(email, name, pictureUrl, googleId);
                return ResponseEntity.ok(authResponse);
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid Google ID token"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Google auth verification failed: " + e.getMessage()));
        }
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
