package com.pesitwizard.client.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pesitwizard.client.entity.BusinessCalendar;

public interface BusinessCalendarRepository extends JpaRepository<BusinessCalendar, String> {
    Optional<BusinessCalendar> findByName(String name);

    Optional<BusinessCalendar> findByDefaultCalendarTrue();
}
