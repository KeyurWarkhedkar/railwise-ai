package com.keyur.railwiseai.services;

import com.keyur.railwiseai.dtos.DirectRoute;
import com.keyur.railwiseai.dtos.ViaRoute;
import com.keyur.railwiseai.entities.SeatAvailability;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.keyur.railwiseai.enums.AvailabilityStatus.WL;

@Service
@Slf4j
public class HardFilterService {

    @Autowired
    private DataProviderService dataProviderService;

    public List<DirectRoute> filterDirectRoutes(List<DirectRoute> routes, LocalDate date) {
        return routes.stream()
                .filter(route -> isAvailabilityAcceptable(route.getTrainNumber(), date))
                .collect(Collectors.toList());
    }

    public List<ViaRoute> filterViaRoutes(List<ViaRoute> routes, LocalDate date) {
        return routes.stream()
                .filter(route -> route.getConnectionBufferMinutes() >= 30)
                .collect(Collectors.toList());
    }

    private boolean isAvailabilityAcceptable(String trainNumber, LocalDate date) {
        try {
            SeatAvailability availability = dataProviderService
                    .getAvailability(trainNumber, date, "3A");

            if (availability == null) return false;

            // Reject if waitlist is too deep
            if (availability.getStatus() == WL && availability.getCount() > 50) return false;

            return true;

        } catch (Exception e) {
            log.warn("Could not fetch availability for {}. Rejecting route.", trainNumber);
            return false; // can't confirm → reject safely
        }
    }
}
