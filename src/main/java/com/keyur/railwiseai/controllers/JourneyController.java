package com.keyur.railwiseai.controllers;

import com.keyur.railwiseai.dtos.JourneyResponse;
import com.keyur.railwiseai.services.JourneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/journey")
public class JourneyController {

    @Autowired
    private JourneyService journeyService;

    @GetMapping
    public JourneyResponse getRoutes(
            @RequestParam String src,
            @RequestParam String dst,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return journeyService.findTopRoutes(src, dst, date);
    }
}
