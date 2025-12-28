package com.pesitwizard.client.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pesitwizard.client.security.SecretsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API for security/encryption configuration in the client.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/security")
@RequiredArgsConstructor
public class SecurityController {

    private final SecretsService secretsService;

    /**
     * Get current encryption status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        var status = secretsService.getStatus();
        return ResponseEntity.ok(Map.of(
                "encryption", Map.of(
                        "enabled", status.enabled(),
                        "mode", status.mode(),
                        "message", status.message())));
    }

    /**
     * Generate a new master key (for display only - user must set it as env var)
     */
    @PostMapping("/generate-key")
    public ResponseEntity<Map<String, Object>> generateKey() {
        byte[] key = new byte[32];
        new java.security.SecureRandom().nextBytes(key);
        String base64Key = java.util.Base64.getEncoder().encodeToString(key);

        return ResponseEntity.ok(Map.of(
                "key", base64Key,
                "instructions",
                "Set this key as environment variable PESITWIZARD_SECURITY_MASTER_KEY and restart the application."));
    }

    /**
     * Test if a value can be encrypted/decrypted
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testEncryption(@RequestBody Map<String, String> request) {
        String testValue = request.getOrDefault("value", "test-encryption-value");

        try {
            if (!secretsService.isAvailable()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message",
                        "Encryption not configured. Set PESITWIZARD_SECURITY_MASTER_KEY environment variable."));
            }

            String encrypted = secretsService.encrypt(testValue);
            String decrypted = secretsService.decrypt(encrypted);
            boolean success = testValue.equals(decrypted);

            return ResponseEntity.ok(Map.of(
                    "success", success,
                    "message", success ? "Encryption working correctly" : "Decryption mismatch",
                    "encrypted", encrypted.substring(0, Math.min(20, encrypted.length())) + "..."));
        } catch (Exception e) {
            log.error("Encryption test failed: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Encryption test failed: " + e.getMessage()));
        }
    }
}
