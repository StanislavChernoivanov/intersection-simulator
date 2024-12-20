package com.schernoivanov.intersectionSimulator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrafficLightListDTO {

    private List<TrafficLightDTO> trafficLights;
}
