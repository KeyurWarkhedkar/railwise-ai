package com.keyur.railwiseai.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class DirectRoute {
    private String trainNumber;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private int dayOffset;
    private LocalDate travelDate;
}
