package com.pesitwizard.client.security;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for encrypting/decrypting sensitive data in the client application.
 * Uses AES-256-GCM encryption with a master key from environment/config.
 * 
 * Format: "ENC:" + BASE64(IV + CIPHERTEXT + AUTH_TAG)
 */
@Slf4j
@Service
public class SecretsService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int KEY_LENGTH = 256;
    private static final int ITERATIONS = 65536;
    private static final String PREFIX = "ENC:";
    private static final String SALT = "PeSITWizardClient";

    private final SecretKey secretKey;
    private final boolean available;

    public SecretsService(@Value("${pesitwizard.security.master-key:}") String masterKey) {
        SecretKey key = null;
        boolean avail = false;

        if (masterKey != null && !masterKey.isBlank()) {
            try {
                key = deriveKey(masterKey);
                avail = true;
                log.info("Client secrets encryption initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize encryption: {}", e.getMessage());
            }
        } else {
            log.warn("No master key configured (PESITWIZARD_SECURITY_MASTER_KEY). " +
                    "Encryption will not be available. Sensitive data will be stored in plaintext.");
        }

        this.secretKey = key;
        this.available = avail;
    }

    private SecretKey deriveKey(String masterKey) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(
                masterKey.toCharArray(),
                SALT.getBytes(StandardCharsets.UTF_8),
                ITERATIONS,
                KEY_LENGTH);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Encrypt a sensitive value before storing in database.
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) {
            return plaintext;
        }

        // Don't re-encrypt
        if (isEncrypted(plaintext)) {
            return plaintext;
        }

        if (!available) {
            log.debug("Encryption not available, storing plaintext");
            return plaintext;
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return PREFIX + Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            log.error("Encryption failed: {}", e.getMessage());
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt a value retrieved from database.
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isBlank()) {
            return ciphertext;
        }

        // Not encrypted
        if (!isEncrypted(ciphertext)) {
            return ciphertext;
        }

        if (!available) {
            log.warn("Cannot decrypt: encryption not available");
            return ciphertext;
        }

        try {
            String encoded = ciphertext.substring(PREFIX.length());
            byte[] combined = Base64.getDecoder().decode(encoded);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plaintext = cipher.doFinal(encrypted);
            return new String(plaintext, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage());
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Check if a value is encrypted.
     */
    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    /**
     * Check if encryption is available.
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Get encryption status for UI display.
     */
    public EncryptionStatus getStatus() {
        if (available) {
            return new EncryptionStatus(true, "AES-256-GCM", "Encryption active");
        } else {
            return new EncryptionStatus(false, "NONE", "No encryption configured");
        }
    }

    public record EncryptionStatus(boolean enabled, String mode, String message) {
    }
}
