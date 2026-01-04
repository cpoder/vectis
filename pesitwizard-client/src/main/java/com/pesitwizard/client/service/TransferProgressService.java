package com.pesitwizard.client.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for sending real-time transfer progress updates via WebSocket.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferProgressService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send progress update to WebSocket subscribers.
     * Clients subscribe to /topic/transfer/{transferId}/progress
     */
    public void sendProgress(String transferId, long bytesTransferred, long fileSize, int syncPoint) {
        if (transferId == null) {
            return;
        }

        String bytesFormatted = formatBytes(bytesTransferred);
        String sizeFormatted = fileSize > 0 ? formatBytes(fileSize) : "unknown";
        TransferProgressMessage message = new TransferProgressMessage(
                transferId,
                bytesTransferred,
                fileSize,
                fileSize > 0 ? (int) ((bytesTransferred * 100) / fileSize) : -1, // -1 = unknown
                syncPoint,
                "IN_PROGRESS",
                null,
                bytesFormatted,
                sizeFormatted);

        String destination = "/topic/transfer/" + transferId + "/progress";
        log.info("Sending WebSocket progress to {}: {} / {} ({}%)",
                destination, bytesFormatted, sizeFormatted, message.percentage());
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * Send transfer completion notification.
     */
    public void sendComplete(String transferId, long bytesTransferred, long fileSize) {
        if (transferId == null) {
            return;
        }

        String bytesFormatted = formatBytes(bytesTransferred);
        String sizeFormatted = fileSize > 0 ? formatBytes(fileSize) : bytesFormatted;
        TransferProgressMessage message = new TransferProgressMessage(
                transferId,
                bytesTransferred,
                fileSize,
                100,
                0,
                "COMPLETED",
                null,
                bytesFormatted,
                sizeFormatted);

        String destination = "/topic/transfer/" + transferId + "/progress";
        messagingTemplate.convertAndSend(destination, message);
        log.info("WebSocket: transfer {} completed ({})", transferId, bytesFormatted);
    }

    /**
     * Send transfer failure notification.
     */
    public void sendFailed(String transferId, String errorMessage) {
        if (transferId == null) {
            return;
        }

        TransferProgressMessage message = new TransferProgressMessage(
                transferId,
                0,
                0,
                0,
                0,
                "FAILED",
                errorMessage,
                "0 B",
                "unknown");

        String destination = "/topic/transfer/" + transferId + "/progress";
        messagingTemplate.convertAndSend(destination, message);
        log.info("WebSocket: transfer {} failed - {}", transferId, errorMessage);
    }

    /**
     * Format bytes to human-readable string (B, KB, MB, GB).
     */
    private static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * Progress message record for WebSocket communication.
     */
    public record TransferProgressMessage(
            String transferId,
            long bytesTransferred,
            long fileSize,
            int percentage,
            int lastSyncPoint,
            String status,
            String errorMessage,
            String bytesTransferredFormatted,
            String fileSizeFormatted) {
    }
}
