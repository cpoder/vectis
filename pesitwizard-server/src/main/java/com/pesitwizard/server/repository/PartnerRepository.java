package com.pesitwizard.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pesitwizard.server.entity.Partner;

/**
 * Repository for Partner entities.
 */
@Repository
public interface PartnerRepository extends JpaRepository<Partner, String> {

    List<Partner> findByEnabled(boolean enabled);

    List<Partner> findByAccessType(Partner.AccessType accessType);
}
