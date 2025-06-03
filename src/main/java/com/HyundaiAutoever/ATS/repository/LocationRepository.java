package com.HyundaiAutoever.ATS.repository;

import com.HyundaiAutoever.ATS.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByName(String name);
    List<Location> findByIsActiveTrue();
    boolean existsByName(String name);
} 