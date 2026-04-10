package com.keyur.railwiseai.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScoredRoute {

    private String routeType;   // DIRECT / VIA

    private int score;

    private String reason;

    private Object route; // DirectRoute or ViaRoute
}
