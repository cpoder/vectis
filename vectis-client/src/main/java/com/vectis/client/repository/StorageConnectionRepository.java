package com.vectis.client.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vectis.client.entity.StorageConnection;

@Repository
public interface StorageConnectionRepository extends JpaRepository<StorageConnection, String> {

    Optional<StorageConnection> findByName(String name);

    List<StorageConnection> findByConnectorType(String connectorType);

    List<StorageConnection> findByEnabled(boolean enabled);

    boolean existsByName(String name);
}
