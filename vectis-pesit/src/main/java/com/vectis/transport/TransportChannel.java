package com.vectis.transport;

import java.io.IOException;

/**
 * Transport layer abstraction for PESIT protocol
 * Supports TCP/IP, SSL/TLS, and potentially X.25
 */
public interface TransportChannel {
    
    /**
     * Connect to remote endpoint
     */
    void connect() throws IOException;
    
    /**
     * Send data over the transport
     */
    void send(byte[] data) throws IOException;
    
    /**
     * Receive data from the transport
     * @return received data
     */
    byte[] receive() throws IOException;
    
    /**
     * Check if transport is connected
     */
    boolean isConnected();
    
    /**
     * Close the transport connection
     */
    void close() throws IOException;
    
    /**
     * Get remote address
     */
    String getRemoteAddress();
    
    /**
     * Get local address
     */
    String getLocalAddress();
    
    /**
     * Check if transport uses SSL/TLS
     */
    boolean isSecure();
    
    /**
     * Set timeout for receive operations (in milliseconds)
     */
    void setReceiveTimeout(int timeoutMs);
    
    /**
     * Get transport type (TCP, SSL, X25, etc.)
     */
    TransportType getTransportType();
}
