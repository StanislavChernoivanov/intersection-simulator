package com.schernoivanov.intersectionSimulator.trafficLight;

import com.schernoivanov.intersectionSimulator.event.Event;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
@Setter
public class TrafficLight {

    private Integer id;

    private int oppositeTrafficLightId;

    private int [] perpendicularTrafficLightIds;

    protected TrafficLightType trafficLightType;

    private RoadNumber roadNumber;

    private  TrafficLightColor color;

    private BlockingQueue<Event<?>> eventQueue;

    @Value("app.queue.default-queue-size")
    private int queueSize;

    private final ScheduledExecutorService scheduledThreadPool;

    public TrafficLight() {

        scheduledThreadPool =
                Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }



    public void addEvent(Event<?> event) {
        eventQueue.add(event);
    }

}
