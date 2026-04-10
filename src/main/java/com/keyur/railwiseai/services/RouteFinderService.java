package com.keyur.railwiseai.services;

import com.keyur.railwiseai.dtos.DirectRoute;
import com.keyur.railwiseai.dtos.ViaRoute;
import com.keyur.railwiseai.entities.TrainSchedule;
import com.keyur.railwiseai.repositories.TrainScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RouteFinderService {

    @Autowired
    private TrainScheduleRepository scheduleRepo;

    public List<DirectRoute> findDirectRoutes(String src, String dst, LocalDate date) {
        // Uses the query we wrote in repository
        List<TrainSchedule> srcStops = scheduleRepo.findDirectTrains(src, dst);

        List<DirectRoute> routes = new ArrayList<>();

        for (TrainSchedule srcStop : srcStops) {
            // Get the destination stop for same train
            TrainSchedule dstStop = scheduleRepo
                    .findByTrainNumberAndStationCode(srcStop.getTrainNumber(), dst)
                    .orElseThrow(() -> new RuntimeException("No destination station found"));

            DirectRoute route = new DirectRoute();
            route.setTrainNumber(srcStop.getTrainNumber());
            route.setDepartureTime(srcStop.getDepartureTime());
            route.setArrivalTime(dstStop.getArrivalTime());
            route.setDayOffset(dstStop.getDayOffset() - srcStop.getDayOffset());
            route.setTravelDate(date);

            routes.add(route);
        }

        return routes;
    }

    public List<ViaRoute> findViaRoutes(String src, String dst, LocalDate date) {
        List<ViaRoute> viaRoutes = new ArrayList<>();

        // All stations reachable from src
        List<TrainSchedule> fromSrc = scheduleRepo.findByStationCode(src);

        for (TrainSchedule leg1SrcStop : fromSrc) {
            String trainNumber1 = leg1SrcStop.getTrainNumber();

            // All stops AFTER src on this train — potential connection points
            List<TrainSchedule> leg1Stops = scheduleRepo
                    .findByTrainNumberOrderByStopSequenceAsc(trainNumber1);

            for (TrainSchedule midStop : leg1Stops) {
                // Must be after src
                if (midStop.getStopSequence() <= leg1SrcStop.getStopSequence()) continue;
                // Don't connect at destination itself
                if (midStop.getStationCode().equals(dst)) continue;

                String mid = midStop.getStationCode();

                // Find trains from mid → dst
                List<TrainSchedule> leg2Trains = scheduleRepo.findDirectTrains(mid, dst);

                for (TrainSchedule leg2MidStop : leg2Trains) {
                    TrainSchedule leg2DstStop = scheduleRepo
                            .findByTrainNumberAndStationCode(leg2MidStop.getTrainNumber(), dst)
                            .orElseThrow(() -> new RuntimeException("No destination station found!"));

                    if (leg2DstStop == null) continue;

                    // Calculate connection buffer
                    int bufferMinutes = calculateBuffer(midStop, leg2MidStop);

                    // Hard filter: negative or too tight buffer → reject
                    if (bufferMinutes < 30) continue;

                    ViaRoute route = new ViaRoute();
                    route.setLeg1TrainNumber(trainNumber1);
                    route.setLeg2TrainNumber(leg2MidStop.getTrainNumber());
                    route.setConnectionStation(mid);
                    route.setConnectionBufferMinutes(bufferMinutes);
                    route.setDepartureTime(leg1SrcStop.getDepartureTime());
                    route.setArrivalTime(leg2DstStop.getArrivalTime());
                    route.setTravelDate(date);

                    viaRoutes.add(route);
                }
            }
        }

        return viaRoutes;
    }

    private int calculateBuffer(TrainSchedule arrival, TrainSchedule departure) {
        // Account for day offset on overnight trains
        int arrivalMins = arrival.getArrivalTime().toSecondOfDay() / 60
                + (arrival.getDayOffset() * 1440);
        int departureMins = departure.getDepartureTime().toSecondOfDay() / 60
                + (departure.getDayOffset() * 1440);
        return departureMins - arrivalMins;
    }
}
