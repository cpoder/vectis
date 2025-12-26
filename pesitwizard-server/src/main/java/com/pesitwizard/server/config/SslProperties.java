package com.pesitwizard.server.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Configuration properties for TLS/SSL.
 */
@Data
@Component
@ConfigurationProperties(prefix = "pesit.ssl")
public class SslProperties {

    /**
     * Enable TLS for PeSIT protocol
     */
    private boolean enabled = false;

    /**
     * Keystore name (from database)
     */
    private String keystoreName = "default-keystore";

    /**
     * Truststore name (from database) - optional
     */
    private String truststoreName;

    /**
     * Client authentication mode for mTLS.
     * NONE = no client cert required
     * WANT = request client cert but don't require it
     * NEED = require valid client certificate (mandatory mTLS)
     */
    private ClientAuthMode clientAuth = ClientAuthMode.NONE;

    /**
     * TLS protocol version
     */
    private String protocol = "TLSv1.3";

    /**
     * Verify client certificate against CA
     */
    private boolean verifyCertificateChain = true;

    public enum ClientAuthMode {
        NONE, // No client certificate required
        WANT, // Request but don't require
        NEED // Mandatory client certificate (mTLS)
    }

    /**
     * Enabled cipher suites (empty = use defaults)
     */
    private List<String> cipherSuites = new ArrayList<>();
}
