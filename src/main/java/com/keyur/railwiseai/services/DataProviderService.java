package com.keyur.railwiseai.services;

import com.keyur.railwiseai.data_providers.LiveProvider;
import com.keyur.railwiseai.data_providers.MockProvider;
import com.keyur.railwiseai.entities.SeatAvailability;
import com.keyur.railwiseai.entities.TrainDelay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
public class DataProviderService {

    @Autowired
    @Qualifier("liveProvider")
    private LiveProvider liveProvider;

    @Autowired
    @Qualifier("mockProvider")
    private MockProvider mockProvider;

    public SeatAvailability getAvailability(String trainNumber, LocalDate date, String seatClass) {
        try {
            return liveProvider.getAvailability(trainNumber, date, seatClass);
        } catch (Exception e) {
            return mockProvider.getAvailability(trainNumber, date, seatClass);
        }
    }

    public TrainDelay getDelay(String trainNumber, String stationCode) {
        try {
            return liveProvider.getDelay(trainNumber, stationCode);
        } catch (Exception e) {
            return mockProvider.getDelay(trainNumber, stationCode);
        }
    }
}
