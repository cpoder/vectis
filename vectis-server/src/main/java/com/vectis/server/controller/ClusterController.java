package com.vectis.server.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vectis.server.cluster.ClusterProvider;
import com.vectis.server.dto.ClusterStatusResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API controller for cluster management and status.
 */
@Slf4j
@RestController
@RequestMapping("/api/cluster")
@RequiredArgsConstructor
public class ClusterController {

    private final ClusterProvider clusterProvider;

    /**
     * Get cluster status
     */
    @GetMapping("/status")
    public ClusterStatusResponse getClusterStatus() {
        ClusterStatusResponse response = new ClusterStatusResponse();
        response.setClusterEnabled(clusterProvider.isClusterEnabled());
        response.setNodeName(clusterProvider.getNodeName());
        response.setLeader(clusterProvider.isLeader());
        response.setConnected(clusterProvider.isConnected());
        response.setClusterSize(clusterProvider.getClusterSize());
        response.setMembers(clusterProvider.getClusterMembers());
        response.setServerOwnership(clusterProvider.getAllServerOwnership());
        return response;
    }

    /**
     * Get cluster members
     */
    @GetMapping("/members")
    public ResponseEntity<?> getClusterMembers() {
        return ResponseEntity.ok(Map.of(
                "members", clusterProvider.getClusterMembers(),
                "size", clusterProvider.getClusterSize(),
                "leader", clusterProvider.isLeader()));
    }

    /**
     * Get server ownership across cluster
     */
    @GetMapping("/ownership")
    public ResponseEntity<?> getServerOwnership() {
        return ResponseEntity.ok(clusterProvider.getAllServerOwnership());
    }

    /**
     * Check if this node is the leader
     */
    @GetMapping("/leader")
    public ResponseEntity<?> isLeader() {
        return ResponseEntity.ok(Map.of(
                "nodeName", clusterProvider.getNodeName(),
                "isLeader", clusterProvider.isLeader()));
    }
}
