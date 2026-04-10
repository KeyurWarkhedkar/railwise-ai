package com.keyur.railwiseai.services;

import com.keyur.railwiseai.dtos.DirectRoute;
import com.keyur.railwiseai.dtos.JourneyResponse;
import com.keyur.railwiseai.dtos.ScoredRoute;
import com.keyur.railwiseai.dtos.ViaRoute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JourneyService {

    @Autowired
    private RouteFinderService routeFinderService;

    @Autowired
    private HardFilterService hardFilterService;

    @Autowired
    private ScoringEngine scoringEngine;

    @Cacheable(value = "journey_results", key = "#src + '-' + #dst + '-' + #date")
    public JourneyResponse findTopRoutes(String src, String dst, LocalDate date) {

        log.info("Computing journey for {} -> {} on {}. Cache Miss", src, dst, date);

        // Find all possible routes
        List<DirectRoute> directRoutes = routeFinderService.findDirectRoutes(src, dst, date);
        List<ViaRoute> viaRoutes = routeFinderService.findViaRoutes(src, dst, date);

        // Hard filter
        List<DirectRoute> filteredDirect = hardFilterService.filterDirectRoutes(directRoutes, date);
        List<ViaRoute> filteredVia = hardFilterService.filterViaRoutes(viaRoutes, date);

        // Score and rank
        List<ScoredRoute> scoredRoutes = scoringEngine.score(filteredDirect, filteredVia, date);

        // Return top 3
        return new JourneyResponse(
                scoredRoutes.stream()
                        .sorted(Comparator.comparingInt(ScoredRoute::getScore).reversed())
                        .limit(3)
                        .collect(Collectors.toList())
        );
    }
}
