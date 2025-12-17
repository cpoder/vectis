package com.pesitwizard.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import lombok.extern.slf4j.Slf4j;

/**
 * TCP/IP transport implementation for PESIT protocol
 */
@Slf4j
public class TcpTransportChannel implements TransportChannel {

    private static final int DEFAULT_TIMEOUT = 30000; // 30 seconds

    private final String host;
    private final int port;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private int receiveTimeout = DEFAULT_TIMEOUT;

    public TcpTransportChannel(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void connect() throws IOException {
        if (socket != null && socket.isConnected()) {
            log.warn("Already connected to {}:{}", host, port);
            return;
        }

        log.info("Connecting to {}:{}", host, port);

        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(receiveTimeout);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);

            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            log.info("Successfully connected to {}:{}", host, port);

        } catch (IOException e) {
            log.error("Failed to connect to {}:{}", host, port, e);
            throw e;
        }
    }

    @Override
    public void send(byte[] data) throws IOException {
        if (!isConnected()) {
            throw new IOException("Not connected");
        }

        try {
            // Send length prefix (4 bytes) followed by data
            outputStream.writeShort(data.length);
            outputStream.write(data);
            outputStream.flush();

            log.debug("Sent {} bytes to {}:{}", data.length, host, port);

        } catch (IOException e) {
            log.error("Error sending data to {}:{}", host, port, e);
            throw e;
        }
    }

    @Override
    public byte[] receive() throws IOException {
        if (!isConnected()) {
            throw new IOException("Not connected");
        }

        try {
            // Read length prefix (4 bytes)
            int length = inputStream.readUnsignedShort();

            // Read data
            byte[] data = new byte[length];
            inputStream.readFully(data);

            log.debug("Received {} bytes from {}:{}", length, host, port);
            return data;

        } catch (SocketTimeoutException e) {
            log.debug("Receive timeout on {}:{}", host, port);
            throw e;
        } catch (IOException e) {
            log.error("Error receiving data from {}:{}", host, port, e);
            throw e;
        }
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public void close() throws IOException {
        log.info("Closing connection to {}:{}", host, port);

        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } finally {
            socket = null;
            inputStream = null;
            outputStream = null;
        }
    }

    @Override
    public String getRemoteAddress() {
        return socket != null ? socket.getRemoteSocketAddress().toString() : host + ":" + port;
    }

    @Override
    public String getLocalAddress() {
        return socket != null ? socket.getLocalSocketAddress().toString() : "not connected";
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public void setReceiveTimeout(int timeoutMs) {
        this.receiveTimeout = timeoutMs;
        if (socket != null) {
            try {
                socket.setSoTimeout(timeoutMs);
            } catch (IOException e) {
                log.warn("Failed to set socket timeout", e);
            }
        }
    }

    @Override
    public TransportType getTransportType() {
        return TransportType.TCP;
    }
}
