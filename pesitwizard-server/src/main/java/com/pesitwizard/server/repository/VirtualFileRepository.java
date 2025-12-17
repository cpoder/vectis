package com.pesitwizard.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pesitwizard.server.entity.VirtualFile;

/**
 * Repository for VirtualFile entities.
 */
@Repository
public interface VirtualFileRepository extends JpaRepository<VirtualFile, String> {

    List<VirtualFile> findByEnabled(boolean enabled);

    List<VirtualFile> findByDirection(VirtualFile.Direction direction);
}
