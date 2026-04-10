package com.keyur.railwiseai.data_providers;

import com.keyur.railwiseai.entities.SeatAvailability;
import com.keyur.railwiseai.entities.TrainDelay;
import com.keyur.railwiseai.enums.DelaySource;
import org.springframework.stereotype.Component;
import com.keyur.railwiseai.enums.AvailabilityStatus;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static com.keyur.railwiseai.enums.AvailabilityStatus.*;

@Component("mockProvider")
public class MockProvider {

    public SeatAvailability getAvailability(String trainNumber, LocalDate date, String seatClass) {

        Map<String, SeatAvailability> mockData = new HashMap<>();

        mockData.put("12001", new SeatAvailability("12001", date, "3A", AVL, 25));
        mockData.put("12002", new SeatAvailability("12002", date, "3A", RAC, 5));
        mockData.put("12003", new SeatAvailability("12003", date, "3A", WL, 10));
        mockData.put("12004", new SeatAvailability("12004", date, "3A", AVL, 40));
        mockData.put("12005", new SeatAvailability("12005", date, "3A", WL, 60));

        return mockData.getOrDefault(
                trainNumber,
                new SeatAvailability(trainNumber, date, seatClass, AVL, 15)
        );
    }

    public TrainDelay getDelay(String trainNumber, String stationCode) {

        Map<String, Integer> delayData = new HashMap<>();

        delayData.put("12001", 5);
        delayData.put("12002", 15);
        delayData.put("12003", 35);
        delayData.put("12004", 10);
        delayData.put("12005", 60);

        return TrainDelay.builder()
                .trainNumber(trainNumber)
                .stationCode(stationCode)
                .delayMinutes(delayData.getOrDefault(trainNumber, 10))
                .source(DelaySource.ESTIMATED)
                .date(dateTodaySafe())
                .build();
    }

    private LocalDate dateTodaySafe() {
        return LocalDate.now();
    }
}