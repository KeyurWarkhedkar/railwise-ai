package com.keyur.railwiseai.entities;

import com.keyur.railwiseai.enums.TrainType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Train {
    @Id
    private String trainNumber;
    private String trainName;
    @Enumerated(EnumType.STRING)
    private TrainType type;
    private String originStation;
    private String destinationStation;
    private int totalDurationMinutes;
}
