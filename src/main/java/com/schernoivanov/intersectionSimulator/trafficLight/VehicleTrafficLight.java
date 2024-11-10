package com.schernoivanov.intersectionSimulator.trafficLight;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VehicleTrafficLight extends TrafficLight {


    private int[] nearestPedestrianTrafficLightsIds;


    public VehicleTrafficLight() {

        trafficLightType = TrafficLightType.VEHICLE;
    }
}
