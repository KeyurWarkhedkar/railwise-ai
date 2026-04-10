package com.keyur.railwiseai.entities;

import com.keyur.railwiseai.enums.DelaySource;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainDelay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trainNumber;
    private LocalDate date;
    private String stationCode;
    private int delayMinutes;
    @Enumerated(EnumType.STRING)
    private DelaySource source;
}
