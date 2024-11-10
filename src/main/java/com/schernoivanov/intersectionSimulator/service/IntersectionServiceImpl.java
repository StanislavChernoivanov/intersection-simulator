package com.schernoivanov.intersectionSimulator.service;

import com.schernoivanov.intersectionSimulator.event.ChangeQueueSizeEvent;
import com.schernoivanov.intersectionSimulator.event.Event;
import com.schernoivanov.intersectionSimulator.handler.EventHandler;
import com.schernoivanov.intersectionSimulator.handler.EventScheduler;
import com.schernoivanov.intersectionSimulator.handler.EventsConsumerStarter;
import com.schernoivanov.intersectionSimulator.trafficLight.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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

        TrafficLight trafficLight = trafficLights.values()
                .stream()
                .filter(v -> Objects.equals(v.getId(), id))
                .findAny()
                .orElseThrow( () -> new RuntimeException(
                        MessageFormat.format(
                                "TrafficLight with id={0} is not found"
                                , id
                        )
                ));

        trafficLight.setTimer(getTrafficLightTimerById(trafficLight));

        return trafficLight;
    }

    @Override
    public void changeTrafficLightColorById(Integer id, String newColor) {

        TrafficLight trafficLight = getTrafficLightById(id);

        try {
            eventScheduler.lightingControlScheduling(trafficLight, TrafficLightColor.valueOf(newColor));
        } catch (InterruptedException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public TrafficLight changeQueueSizeById(Integer id, int queueSize) {

        TrafficLight trafficLight = getTrafficLightById(id);

        Event<Integer> changeQueueSizeEvent = new ChangeQueueSizeEvent(id, id, queueSize);

        trafficLight.addEvent(changeQueueSizeEvent);

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
        }

        trafficLight.setQueueSize(queueSize);

        return trafficLight;
    }


    private String getTrafficLightTimerById(TrafficLight trafficLight) {

        int duration = trafficLight.getTrafficLightColorDuration();

        trafficLight.getStopWatch().split();

        return MessageFormat.format("Осталось {0} cек",
            duration - trafficLight.getStopWatch().getSplitDuration().toSeconds()
        );
    }


}
