package com.schernoivanov.intersectionSimulator.trafficLight;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PedestrianTrafficLight extends TrafficLight {

    private int parallelTrafficLightId;

    private int nearestVehicleTrafficLightId;

    public PedestrianTrafficLight() {

        trafficLightType = TrafficLightType.PEDESTRIAN;
    }
}
