package com.schernoivanov.intersectionSimulator.dto;

import com.schernoivanov.intersectionSimulator.trafficLight.RoadNumber;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLightColor;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLightType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrafficLightDTO {

    private Integer id;

    private TrafficLightType trafficLightType;

    private RoadNumber roadNumber;

    private TrafficLightColor color;
}
