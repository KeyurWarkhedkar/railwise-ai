package com.keyur.railwiseai.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ViaRoute {
    private String leg1TrainNumber;
    private String leg2TrainNumber;
    private String connectionStation;
    private int connectionBufferMinutes;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private LocalDate travelDate;
}
