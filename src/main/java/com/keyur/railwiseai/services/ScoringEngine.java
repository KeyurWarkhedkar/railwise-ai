package com.keyur.railwiseai.services;

import com.keyur.railwiseai.dtos.DirectRoute;
import com.keyur.railwiseai.dtos.ViaRoute;
import com.keyur.railwiseai.dtos.ScoredRoute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ScoringEngine {

    private final DataProviderService dataProviderService;

    public ScoringEngine(DataProviderService dataProviderService) {
        this.dataProviderService = dataProviderService;
    }

    public List<ScoredRoute> score(
            List<DirectRoute> directRoutes,
            List<ViaRoute> viaRoutes,
            LocalDate date
    ) {

        List<ScoredRoute> result = new ArrayList<>();


        for (DirectRoute route : directRoutes) {

            int availabilityScore = getAvailabilityScore(route.getTrainNumber(), date);
            int delayScore = getDelayScore(route.getTrainNumber());

            int totalScore = availabilityScore + delayScore;

            result.add(new ScoredRoute(
                    "DIRECT",
                    totalScore,
                    buildReason(availabilityScore, delayScore, 0),
                    route
            ));
        }


        for (ViaRoute route : viaRoutes) {

            int leg1Availability = getAvailabilityScore(route.getLeg1TrainNumber(), date);
            int leg2Availability = getAvailabilityScore(route.getLeg2TrainNumber(), date);

            int leg1Delay = getDelayScore(route.getLeg1TrainNumber());
            int leg2Delay = getDelayScore(route.getLeg2TrainNumber());

            int connectionScore = getConnectionScore(route.getConnectionBufferMinutes());

            int totalScore =
                    leg1Availability + leg2Availability +
                            leg1Delay + leg2Delay +
                            connectionScore - 5; // As a penalty for VIA routes

            result.add(new ScoredRoute(
                    "VIA",
                    totalScore,
                    buildReason(
                            leg1Availability + leg2Availability,
                            leg1Delay + leg2Delay,
                            connectionScore
                    ),
                    route
            ));
        }

        return result;
    }

    private int getAvailabilityScore(String trainNo, LocalDate date) {

        var availability = dataProviderService
                .getAvailability(trainNo, date, "3A");

        if (availability == null) return 0;

        switch (availability.getStatus()) {
            case AVL: return 50;
            case RAC: return 30;
            case WL:
                return availability.getCount() <= 20 ? 10 : 0;
            default:
                return 0;
        }
    }

    private int getDelayScore(String trainNo) {

        int delay = dataProviderService
                .getDelay(trainNo, "UNKNOWN")
                .getDelayMinutes();

        if (delay <= 5) return 30;
        if (delay <= 15) return 20;
        if (delay <= 30) return 10;

        return 0;
    }

    private int getConnectionScore(int bufferMinutes) {

        if (bufferMinutes >= 60) return 20;
        if (bufferMinutes >= 30) return 10;

        return 0;
    }


    private String buildReason(int availability, int delay, int connection) {

        if (availability >= 80 && delay >= 20)
            return "High availability and low delay";

        if (connection > 0)
            return "Safe connection with acceptable travel conditions";

        if (delay == 0)
            return "High delay detected";

        return "Balanced route";
    }
}