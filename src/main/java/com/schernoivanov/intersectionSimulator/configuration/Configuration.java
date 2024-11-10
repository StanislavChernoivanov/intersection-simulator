package com.schernoivanov.intersectionSimulator.configuration;
import com.schernoivanov.intersectionSimulator.trafficLight.*;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@org.springframework.context.annotation.Configuration
public class Configuration {


    @Value("${app.traffic-light.default-green-color-duration}")
    private int greenColorDuration;
    @Value("${app.traffic-light.default-red-color-duration}")
    private int redColorDuration;
    @Value("${app.traffic-light.default-yellow-color-duration}")
    private int yellowColorDuration;
    @Value("${app.queue.default-queue-size}")
    private int defaultQueueSize;


    @Bean
    public Map<String, Integer> trafficLightColorDurations() {

        Map<String, Integer> trafficLightColorDurations = new HashMap<>();

        trafficLightColorDurations.put("red13", redColorDuration);
        trafficLightColorDurations.put("red24", redColorDuration);
        trafficLightColorDurations.put("red57911", redColorDuration);
        trafficLightColorDurations.put("red681012", redColorDuration);
        trafficLightColorDurations.put("green1357911", greenColorDuration);
        trafficLightColorDurations.put("green24681012", greenColorDuration);
        trafficLightColorDurations.put("yellow", yellowColorDuration);

        return trafficLightColorDurations;
    }

    @Bean
    public Map<Integer, TrafficLight> trafficLights() {
        Map<Integer, TrafficLight> trafficLights = new ConcurrentHashMap<>();

        trafficLightsInit(trafficLights);
        setDependencies(trafficLights);

        return trafficLights;
    }


    private void trafficLightsInit(Map<Integer, TrafficLight> trafficLights) {
        for (int i = 1; i < 5; i++) {

            TrafficLight trafficLight = getVehicleTrafficLight(i);

            trafficLights.put(i, trafficLight);
        }

        for (int i = 5; i < 13; i++) {

            TrafficLight trafficLight = getPedestrianTrafficLight(i);

            trafficLights.put(i, trafficLight);
        }
    }

    private TrafficLight getVehicleTrafficLight(int i) {
        TrafficLight trafficLight = new VehicleTrafficLight();
        trafficLight.setId(i);
        trafficLight.setEventQueue(new LinkedBlockingQueue<>());
        trafficLight.setTrafficLightType(TrafficLightType.VEHICLE);
        trafficLight.setColor(TrafficLightColor.DISABLED);
        trafficLight.setQueueSize(defaultQueueSize);
        trafficLight.setStopWatch(StopWatch.createStarted());

        if (i % 2 != 0) {
            trafficLight.setRoadNumber(RoadNumber.FIRST);
        }
        else {
            trafficLight.setRoadNumber(RoadNumber.SECOND);
        }
        return trafficLight;
    }

    private TrafficLight getPedestrianTrafficLight(int i) {
        TrafficLight trafficLight = new PedestrianTrafficLight();
        trafficLight.setId(i);
        trafficLight.setEventQueue(new LinkedBlockingQueue<>());
        trafficLight.setTrafficLightType(TrafficLightType.PEDESTRIAN);
        trafficLight.setColor(TrafficLightColor.DISABLED);
        trafficLight.setQueueSize(defaultQueueSize);
        trafficLight.setStopWatch(StopWatch.createStarted());

        if (i % 2 != 0) {
            trafficLight.setRoadNumber(RoadNumber.FIRST);
        } else {
            trafficLight.setRoadNumber(RoadNumber.SECOND);
        }

        return trafficLight;
    }



    private void setDependencies(Map<Integer, TrafficLight> trafficLights) {
        trafficLights.values().forEach(v -> {
            switch (v.getId()) {
                case 1:
                    setDependenciesForVehicleTrafficLight(
                            v,
                            new int[]{5, 7},
                            3,
                            new int[]{2, 4}
                    );
                    break;

                case 2:
                    setDependenciesForVehicleTrafficLight(
                            v,
                            new int[]{6, 10},
                            4,
                            new int[]{1, 3}
                    );
                    break;

                case 3:
                    setDependenciesForVehicleTrafficLight(
                            v,
                            new int[]{9, 11},
                            1,
                            new int[]{2, 4}
                    );
                    break;

                case 4:
                    setDependenciesForVehicleTrafficLight(
                            v,
                            new int[]{8, 12},
                            2,
                            new int[]{1, 3}
                    );
                    break;

                case 5:
                    setDependenciesForPedestrianTrafficLight(
                            v, 7, new int[]{6, 8}, 9, 1
                    );
                    break;

                case 6:
                    setDependenciesForPedestrianTrafficLight(
                            v, 10, new int[]{5, 9}, 8, 2
                    );
                    break;

                case 7:
                    setDependenciesForPedestrianTrafficLight(
                            v, 5, new int[]{6, 8}, 11, 1
                    );
                    break;

                case 8:
                    setDependenciesForPedestrianTrafficLight(
                            v, 12, new int[]{7, 11}, 6, 4
                    );
                    break;

                case 9:
                    setDependenciesForPedestrianTrafficLight(
                            v, 11, new int[]{10, 12}, 5, 3
                    );
                    break;

                case 10:
                    setDependenciesForPedestrianTrafficLight(
                            v, 6, new int[]{5, 9}, 12, 2
                    );
                    break;

                case 11:
                    setDependenciesForPedestrianTrafficLight(
                            v, 9, new int[]{10, 12}, 7, 3
                    );
                    break;

                case 12:
                    setDependenciesForPedestrianTrafficLight(
                            v, 8, new int[]{7, 11}, 10, 4
                    );
                    break;

                default:
                    break;
            }

        });
    }

    private void setDependenciesForVehicleTrafficLight(
            TrafficLight v,
            int [] nearest,
            int opposite,
            int [] perpendicular) {

        v.setOppositeTrafficLightId(opposite);
        v.setPerpendicularTrafficLightIds(perpendicular);
        ((VehicleTrafficLight) v).setNearestPedestrianTrafficLightsIds(nearest);
    }

    private void setDependenciesForPedestrianTrafficLight(
            TrafficLight v,
            int opposite,
            int [] perpendicular,
            int parallel,
            int nearest) {

        v.setOppositeTrafficLightId(opposite);
        v.setPerpendicularTrafficLightIds(perpendicular);
        ((PedestrianTrafficLight) v).setParallelTrafficLightId(parallel);
        ((PedestrianTrafficLight) v).setNearestVehicleTrafficLightId(nearest);
    }
}
