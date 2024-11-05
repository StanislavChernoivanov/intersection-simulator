package com.schernoivanov.intersectionSimulator.handler;
import com.schernoivanov.intersectionSimulator.event.Event;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLight;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventsConsumerStarter {


    private final Map<Integer, TrafficLight> trafficLights;


    private final EventHandler eventHandler;


    @PostConstruct
    public void startTrafficLightsWorking() {
        trafficLights.values().forEach(tl -> {
            Thread thread = new Thread(() -> {
                while (true) {

                    try {
                        Event<?> event = tl.getEventQueue().take();
                        eventHandler.handleEvent(tl, event);

                    } catch (InterruptedException e) {
                        log.warn(Arrays.toString(e.getStackTrace()));
                    }

                }
            });
            thread.start();
        });
    }




}