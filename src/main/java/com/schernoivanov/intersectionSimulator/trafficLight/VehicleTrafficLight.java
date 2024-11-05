package com.schernoivanov.intersectionSimulator.trafficLight;

import com.schernoivanov.intersectionSimulator.event.Event;
import lombok.*;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.BlockingQueue;

@Setter
@Getter
public class VehicleTrafficLight extends TrafficLight {


    private int [] nearestPedestrianTrafficLightsIds;


    public VehicleTrafficLight() {

         trafficLightType = TrafficLightType.VEHICLE;
    }
}
