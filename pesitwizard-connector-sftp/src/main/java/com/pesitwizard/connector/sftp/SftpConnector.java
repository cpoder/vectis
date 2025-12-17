package com.pesitwizard.connector.sftp;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.pesitwizard.connector.ConfigParameter;
import com.pesitwizard.connector.ConnectorException;
import com.pesitwizard.connector.FileMetadata;
import com.pesitwizard.connector.StorageConnector;

public class SftpConnector implements StorageConnector {
    private static final Logger log = LoggerFactory.getLogger(SftpConnector.class);
    private String host, username, password, privateKeyPath, basePath;
    private int port;
    private Session session;
    private ChannelSftp channel;
    private boolean initialized = false;

    @Override public String getType() { return "sftp"; }
    @Override public String getName() { return "SFTP"; }
    @Override public String getVersion() { return "1.0.0"; }

    @Override
    public void initialize(Map<String, String> config) throws ConnectorException {
        host = config.get("host");
        port = Integer.parseInt(config.getOrDefault("port", "22"));
        username = config.get("username");
        password = config.get("password");
        privateKeyPath = config.get("privateKey");
        basePath = config.getOrDefault("basePath", "");
        
        if (host == null) throw new ConnectorException(ConnectorException.ErrorCode.INVALID_CONFIG, "Host required");
        if (username == null) throw new ConnectorException(ConnectorException.ErrorCode.INVALID_CONFIG, "Username required");
        
        try {
            JSch jsch = new JSch();
            if (privateKeyPath != null) jsch.addIdentity(privateKeyPath);
            session = jsch.getSession(username, host, port);
            if (password != null) session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(30000);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            initialized = true;
            log.info("SFTP connected: {}@{}:{}", username, host, port);
        } catch (JSchException e) {
            throw new ConnectorException(ConnectorException.ErrorCode.CONNECTION_FAILED, e.getMessage(), e);
        }
    }

    @Override public boolean testConnection() throws ConnectorException {
        checkInit(); try { channel.pwd(); return true; } catch (Exception e) { return false; }
    }
    @Override public boolean exists(String path) throws ConnectorException {
        checkInit(); try { channel.stat(resolve(path)); return true; } catch (SftpException e) { return false; }
    }
    @Override public FileMetadata getMetadata(String path) throws ConnectorException {
        checkInit();
        try {
            SftpATTRS a = channel.stat(resolve(path));
            return FileMetadata.builder().name(path).path(path).size(a.getSize())
                .lastModified(java.time.Instant.ofEpochSecond(a.getMTime())).directory(a.isDir()).build();
        } catch (SftpException e) { throw new ConnectorException("Metadata error", e); }
    }
    @Override @SuppressWarnings("unchecked")
    public List<FileMetadata> list(String path) throws ConnectorException {
        checkInit();
        try {
            List<FileMetadata> r = new ArrayList<>();
            for (ChannelSftp.LsEntry e : (Vector<ChannelSftp.LsEntry>) channel.ls(resolve(path))) {
                if (!e.getFilename().startsWith("."))
                    r.add(FileMetadata.builder().name(e.getFilename()).path(path+"/"+e.getFilename())
                        .size(e.getAttrs().getSize()).directory(e.getAttrs().isDir()).build());
            }
            return r;
        } catch (SftpException e) { throw new ConnectorException("List error", e); }
    }
    @Override public InputStream read(String path) throws ConnectorException {
        checkInit(); try { return channel.get(resolve(path)); } catch (SftpException e) { throw new ConnectorException("Read error", e); }
    }
    @Override public InputStream read(String path, long offset) throws ConnectorException {
        checkInit(); try { return channel.get(resolve(path), null, offset); } catch (SftpException e) { throw new ConnectorException("Read error", e); }
    }
    @Override public OutputStream write(String path) throws ConnectorException { return write(path, false); }
    @Override public OutputStream write(String path, boolean append) throws ConnectorException {
        checkInit(); try { return channel.put(resolve(path), append ? ChannelSftp.APPEND : ChannelSftp.OVERWRITE); } catch (SftpException e) { throw new ConnectorException("Write error", e); }
    }
    @Override public void delete(String path) throws ConnectorException {
        checkInit(); try { channel.rm(resolve(path)); } catch (SftpException e) { /* ignore */ }
    }
    @Override public void mkdir(String path) throws ConnectorException {
        checkInit(); try { channel.mkdir(resolve(path)); } catch (SftpException e) { /* ignore */ }
    }
    @Override public void rename(String src, String dst) throws ConnectorException {
        checkInit(); try { channel.rename(resolve(src), resolve(dst)); } catch (SftpException e) { throw new ConnectorException("Rename error", e); }
    }
    @Override public List<ConfigParameter> getRequiredParameters() {
        return List.of(ConfigParameter.required("host", "SFTP host"), ConfigParameter.required("username", "Username"));
    }
    @Override public List<ConfigParameter> getOptionalParameters() {
        return List.of(ConfigParameter.password("password", "Password"), ConfigParameter.integer("port", "Port", 22));
    }
    @Override public boolean supportsResume() { return true; }
    @Override public void close() { if (channel != null) channel.disconnect(); if (session != null) session.disconnect(); initialized = false; }
    
    private void checkInit() throws ConnectorException { if (!initialized) throw new ConnectorException(ConnectorException.ErrorCode.INVALID_CONFIG, "Not initialized"); }
    private String resolve(String p) { return basePath.isEmpty() ? p : basePath + "/" + p; }
}
