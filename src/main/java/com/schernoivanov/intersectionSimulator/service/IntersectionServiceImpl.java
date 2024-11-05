package com.schernoivanov.intersectionSimulator.service;

import com.schernoivanov.intersectionSimulator.handler.EventScheduler;
import com.schernoivanov.intersectionSimulator.handler.EventsConsumerStarter;
import com.schernoivanov.intersectionSimulator.trafficLight.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntersectionServiceImpl implements IntersectionService{


    private final Map<Integer, TrafficLight> trafficLights;

    private final EventScheduler eventScheduler;


    public List<TrafficLight> getAllTrafficLight() {
        return trafficLights.values().stream().toList();
    }

    public TrafficLight getTrafficLightById(Integer id) {
//        log.info(String.valueOf(trafficLights.size()));
        return trafficLights.values()
                .stream()
                .filter(v -> Objects.equals(v.getId(), id))
                .findAny()
                .orElseThrow( () -> new RuntimeException(
                        MessageFormat.format(
                                "TrafficLight with id={0} is not found"
                                , id
                        )
                ));
    }

    @Override
    public void changeTrafficLightColorById(Integer id, String newColor) {


        TrafficLight trafficLight = getTrafficLightById(id);
        eventScheduler.lightingControlScheduling(trafficLight, TrafficLightColor.valueOf(newColor));
    }

    @Override
    public TrafficLight changeQueueSizeById(Integer id, int queueSize) {

        TrafficLight trafficLight = getTrafficLightById(id);
        trafficLight.setQueueSize(queueSize);

        return trafficLight;
    }


}
