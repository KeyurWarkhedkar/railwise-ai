package com.keyur.railwiseai.services;

import com.keyur.railwiseai.dtos.DirectRoute;
import com.keyur.railwiseai.dtos.ViaRoute;
import com.keyur.railwiseai.dtos.ScoredRoute;
import com.keyur.railwiseai.entities.SeatAvailability;
import com.keyur.railwiseai.entities.TrainDelay;
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

        // --- Score Direct Routes ---
        for (DirectRoute route : directRoutes) {

            SeatAvailability availability = dataProviderService
                    .getAvailability(route.getTrainNumber(), date, "3A");

            TrainDelay delay = dataProviderService
                    .getDelay(route.getTrainNumber(), "UNKNOWN");

            int availabilityScore = scoreAvailability(availability);
            int delayScore = scoreDelay(delay);
            int totalScore = availabilityScore + delayScore;

            String reason = buildReason(
                    availability != null ? availability.getStatus().toString() : "UNKNOWN",
                    delay != null ? delay.getDelayMinutes() : -1,
                    0,
                    false
            );

            result.add(new ScoredRoute("DIRECT", totalScore, reason, route));
        }

        // --- Score Via Routes ---
        for (ViaRoute route : viaRoutes) {

            SeatAvailability leg1Avail = dataProviderService
                    .getAvailability(route.getLeg1TrainNumber(), date, "3A");

            SeatAvailability leg2Avail = dataProviderService
                    .getAvailability(route.getLeg2TrainNumber(), date, "3A");

            TrainDelay leg1Delay = dataProviderService
                    .getDelay(route.getLeg1TrainNumber(), "UNKNOWN");

            TrainDelay leg2Delay = dataProviderService
                    .getDelay(route.getLeg2TrainNumber(), "UNKNOWN");

            // Average both legs so VIA stays on same scale as DIRECT
            int availabilityScore = (scoreAvailability(leg1Avail) + scoreAvailability(leg2Avail)) / 2;
            int delayScore = (scoreDelay(leg1Delay) + scoreDelay(leg2Delay)) / 2;
            int connectionScore = scoreConnection(route.getConnectionBufferMinutes());

            // Subtract 10 as a flat penalty for the extra complexity of a via route
            int totalScore = availabilityScore + delayScore + connectionScore - 10;

            String reason = buildReason(
                    leg1Avail != null ? leg1Avail.getStatus().toString() : "UNKNOWN",
                    leg1Delay != null ? leg1Delay.getDelayMinutes() : -1,
                    route.getConnectionBufferMinutes(),
                    true
            );

            result.add(new ScoredRoute("VIA", totalScore, reason, route));
        }

        return result;
    }

    // --- Scoring Helpers ---

    private int scoreAvailability(SeatAvailability availability) {
        if (availability == null) return 0;
        return switch (availability.getStatus()) {
            case AVL -> 50;
            case RAC -> 30;
            case WL  -> availability.getCount() <= 20 ? 10 : 0;
        };
    }

    private int scoreDelay(TrainDelay delay) {
        if (delay == null) return 0;
        int minutes = delay.getDelayMinutes();
        if (minutes <= 5)  return 30;
        if (minutes <= 15) return 20;
        if (minutes <= 30) return 10;
        return 0;
    }

    private int scoreConnection(int bufferMinutes) {
        if (bufferMinutes >= 60) return 20;
        if (bufferMinutes >= 30) return 10;
        return 0;
    }

    // --- Reason Builder ---

    private String buildReason(String status, int delayMinutes, int bufferMinutes, boolean isVia) {
        StringBuilder reason = new StringBuilder();

        reason.append(status).append(" seat, ");

        if (delayMinutes < 0) {
            reason.append("delay data unavailable");
        } else {
            reason.append(delayMinutes).append(" min delay");
        }

        if (isVia) {
            reason.append(", ").append(bufferMinutes).append(" min connection buffer");
        }

        return reason.toString();
    }
}