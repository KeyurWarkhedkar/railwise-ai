package com.keyur.railwiseai.entities;

import com.keyur.railwiseai.enums.AvailabilityStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatAvailability {
    public SeatAvailability(String trainNumber, LocalDate travelDate,
                            String seatClass, AvailabilityStatus status, int count) {
        this.trainNumber = trainNumber;
        this.travelDate = travelDate;
        this.seatClass = seatClass;
        this.status = status;
        this.count = count;
    }

    @Id
    @GeneratedValue
    private Long id;

    private String trainNumber;
    private LocalDate travelDate;
    private String seatClass;
    private AvailabilityStatus status;
    private int count;
}
