package com.schernoivanov.intersectionSimulator.trafficLight;

import com.schernoivanov.intersectionSimulator.event.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.BlockingQueue;
@Setter
@Getter
public class PedestrianTrafficLight extends TrafficLight {

    private int parallelTrafficLightId;

    private int nearestVehicleTrafficLightId;

    public PedestrianTrafficLight() {

        trafficLightType = TrafficLightType.PEDESTRIAN;
    }
}
