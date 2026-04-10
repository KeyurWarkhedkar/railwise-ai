package com.keyur.railwiseai.repositories;

import com.keyur.railwiseai.entities.TrainSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainScheduleRepository extends JpaRepository<TrainSchedule, Long> {

    // Get all stops for a train
    List<TrainSchedule> findByTrainNumberOrderByStopSequenceAsc(String trainNumber);

    // Get all trains that stop at a given station
    List<TrainSchedule> findByStationCode(String stationCode);

    // Find trains that pass through both stations
    @Query("""
        SELECT s1 FROM TrainSchedule s1
        JOIN TrainSchedule s2 ON s1.trainNumber = s2.trainNumber
        WHERE s1.stationCode = :src
        AND s2.stationCode = :dst
        AND s1.stopSequence < s2.stopSequence
    """)
    List<TrainSchedule> findDirectTrains(
            @Param("src") String srcStation,
            @Param("dst") String dstStation
    );

    Optional<TrainSchedule> findByTrainNumberAndStationCode(String trainNumber, String stationCode);
}
