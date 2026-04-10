package com.keyur.railwiseai.data_providers;

import com.keyur.railwiseai.entities.SeatAvailability;
import com.keyur.railwiseai.entities.TrainDelay;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component("liveProvider")
public class LiveProvider {
    public SeatAvailability getAvailability(String trainNumber, LocalDate date, String seatClass) {
        throw new RuntimeException("Live API unavailable");
    }

    public TrainDelay getDelay(String trainNumber, String stationCode) {
        throw new RuntimeException("Live API unavailable");
    }
}
