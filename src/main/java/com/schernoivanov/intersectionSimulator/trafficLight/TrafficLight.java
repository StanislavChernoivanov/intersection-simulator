package com.schernoivanov.intersectionSimulator.trafficLight;

import com.schernoivanov.intersectionSimulator.event.Event;
import lombok.*;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private int queueSize;

    private final ScheduledExecutorService scheduler;

    private StopWatch stopWatch;

    private short trafficLightColorDuration;

    private int currentlyQueueSize;

    private String timer;




    public TrafficLight() {
        stopWatch = new StopWatch();

        stopWatch.start();


        scheduler =
                Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

        Thread thread = new Thread( () -> {

            currentlyQueueSize = queueSize;

            while( true) {

                while (c==color.equals(TrafficLightColor.GREEN)) {

                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (currentlyQueueSize > 0) {
                        queueSize--;
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }



    public void addEvent(Event<?> event) {
        eventQueue.add(event);
    }

    public void sendEvent(Event<?> event, TrafficLight trafficLight) {
        trafficLight.addEvent(event);
    }

}
