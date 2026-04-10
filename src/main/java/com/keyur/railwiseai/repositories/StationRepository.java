package com.keyur.railwiseai.repositories;

import com.keyur.railwiseai.entities.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, String> {
    Optional<Station> findByStationCodeIgnoreCase(String code);
    List<Station> findByStationNameContainingIgnoreCase(String name);
}
