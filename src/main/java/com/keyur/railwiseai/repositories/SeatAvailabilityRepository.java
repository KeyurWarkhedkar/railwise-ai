package com.keyur.railwiseai.repositories;

import com.keyur.railwiseai.entities.SeatAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SeatAvailabilityRepository extends JpaRepository<SeatAvailability, Long> {

    // Best available class for a train on a date
    List<SeatAvailability> findByTrainNumberAndTravelDate(
            String trainNumber, LocalDate date
    );

    @Query("""
    SELECT s FROM SeatAvailability s
    WHERE s.trainNumber = :trainNumber
    AND s.travelDate = :date
    AND NOT (
        s.status = com.keyur.railwiseai.enums.AvailabilityStatus.WL
        AND s.count > 50
    )
""")
    List<SeatAvailability> findEligibleAvailability(
            @Param("trainNumber") String trainNumber,
            @Param("date") LocalDate date
    );
}
