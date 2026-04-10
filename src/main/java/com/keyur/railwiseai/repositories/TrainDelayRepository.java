package com.keyur.railwiseai.repositories;

import com.keyur.railwiseai.entities.TrainDelay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface TrainDelayRepository extends JpaRepository<TrainDelay, Long> {

    // Get latest delay record for a train at a specific station
    Optional<TrainDelay> findTopByTrainNumberAndStationCodeOrderByDateDesc(
            String trainNumber, String stationCode
    );

    // Get average delay over last N days
    @Query("""
        SELECT AVG(d.delayMinutes) FROM TrainDelay d
        WHERE d.trainNumber = :trainNumber
        AND d.stationCode = :stationCode
        AND d.date >= :since
    """)
    Double findAverageDelay(
            @Param("trainNumber") String trainNumber,
            @Param("stationCode") String stationCode,
            @Param("since") LocalDate since
    );
}
