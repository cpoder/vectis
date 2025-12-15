package com.vectis.connector.s3;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;

import com.vectis.connector.ConfigParameter;
import com.vectis.connector.ConnectorException;
import com.vectis.connector.FileMetadata;
import com.vectis.connector.StorageConnector;

public class S3Connector implements StorageConnector {
    private static final Logger log = LoggerFactory.getLogger(S3Connector.class);
    private S3Client s3;
    private String bucket;
    private String prefix;
    private boolean initialized = false;

    @Override public String getType() { return "s3"; }
    @Override public String getName() { return "AWS S3 / MinIO"; }
    @Override public String getVersion() { return "1.0.0"; }

    @Override
    public void initialize(Map<String, String> config) throws ConnectorException {
        bucket = config.get("bucket");
        prefix = config.getOrDefault("prefix", "");
        String accessKey = config.get("accessKey");
        String secretKey = config.get("secretKey");
        String region = config.getOrDefault("region", "us-east-1");
        String endpoint = config.get("endpoint");

        if (bucket == null) throw new ConnectorException(ConnectorException.ErrorCode.INVALID_CONFIG, "Bucket required");

        try {
            S3ClientBuilder builder = S3Client.builder().region(Region.of(region));
            if (accessKey != null && secretKey != null) {
                builder.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
            }
            if (endpoint != null) {
                builder.endpointOverride(URI.create(endpoint)).forcePathStyle(true);
            }
            s3 = builder.build();
            initialized = true;
            log.info("S3 connected to bucket: {}", bucket);
        } catch (Exception e) {
            throw new ConnectorException(ConnectorException.ErrorCode.CONNECTION_FAILED, e.getMessage(), e);
        }
    }

    @Override public boolean testConnection() throws ConnectorException {
        checkInit(); try { s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build()); return true; } catch (Exception e) { return false; }
    }
    @Override public boolean exists(String path) throws ConnectorException {
        checkInit(); try { s3.headObject(HeadObjectRequest.builder().bucket(bucket).key(resolve(path)).build()); return true; } catch (NoSuchKeyException e) { return false; }
    }
    @Override public FileMetadata getMetadata(String path) throws ConnectorException {
        checkInit();
        try {
            HeadObjectResponse r = s3.headObject(HeadObjectRequest.builder().bucket(bucket).key(resolve(path)).build());
            return FileMetadata.builder().name(path).path(path).size(r.contentLength()).lastModified(r.lastModified()).directory(false).build();
        } catch (NoSuchKeyException e) { throw new ConnectorException(ConnectorException.ErrorCode.FILE_NOT_FOUND, "Not found: " + path); }
    }
    @Override public List<FileMetadata> list(String path) throws ConnectorException {
        checkInit();
        String p = resolve(path); if (!p.isEmpty() && !p.endsWith("/")) p += "/";
        ListObjectsV2Response r = s3.listObjectsV2(ListObjectsV2Request.builder().bucket(bucket).prefix(p).delimiter("/").build());
        List<FileMetadata> result = new ArrayList<>();
        for (S3Object o : r.contents()) {
            String name = o.key().substring(p.length());
            if (!name.isEmpty()) result.add(FileMetadata.builder().name(name).path(o.key()).size(o.size()).lastModified(o.lastModified()).directory(false).build());
        }
        for (CommonPrefix cp : r.commonPrefixes()) {
            String name = cp.prefix().substring(p.length()).replace("/", "");
            result.add(FileMetadata.builder().name(name).path(cp.prefix()).directory(true).build());
        }
        return result;
    }
    @Override public InputStream read(String path) throws ConnectorException {
        checkInit(); return s3.getObject(GetObjectRequest.builder().bucket(bucket).key(resolve(path)).build());
    }
    @Override public InputStream read(String path, long offset) throws ConnectorException {
        checkInit(); return s3.getObject(GetObjectRequest.builder().bucket(bucket).key(resolve(path)).range("bytes=" + offset + "-").build());
    }
    @Override public OutputStream write(String path) throws ConnectorException { return write(path, false); }
    @Override public OutputStream write(String path, boolean append) throws ConnectorException {
        checkInit();
        try {
            PipedInputStream pis = new PipedInputStream();
            PipedOutputStream pos = new PipedOutputStream(pis);
            String key = resolve(path);
            Thread.startVirtualThread(() -> { try { s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(), RequestBody.fromInputStream(pis, -1)); } catch (Exception e) { log.error("S3 upload error", e); } });
            return pos;
        } catch (Exception e) { throw new ConnectorException("Write error", e); }
    }
    @Override public void delete(String path) throws ConnectorException {
        checkInit(); s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(resolve(path)).build());
    }
    @Override public void mkdir(String path) throws ConnectorException { /* S3 doesn't need explicit mkdir */ }
    @Override public void rename(String src, String dst) throws ConnectorException {
        checkInit();
        s3.copyObject(CopyObjectRequest.builder().sourceBucket(bucket).sourceKey(resolve(src)).destinationBucket(bucket).destinationKey(resolve(dst)).build());
        delete(src);
    }
    @Override public List<ConfigParameter> getRequiredParameters() {
        return List.of(ConfigParameter.required("bucket", "S3 bucket name"));
    }
    @Override public List<ConfigParameter> getOptionalParameters() {
        return List.of(ConfigParameter.password("accessKey", "AWS Access Key"), ConfigParameter.password("secretKey", "AWS Secret Key"), ConfigParameter.optional("region", "AWS Region", "us-east-1"), ConfigParameter.optional("endpoint", "Custom endpoint (MinIO)", null));
    }
    @Override public boolean supportsResume() { return true; }
    @Override public void close() { if (s3 != null) s3.close(); initialized = false; }

    private void checkInit() throws ConnectorException { if (!initialized) throw new ConnectorException(ConnectorException.ErrorCode.INVALID_CONFIG, "Not initialized"); }
    private String resolve(String p) { return prefix.isEmpty() ? p : prefix + "/" + p; }
}
