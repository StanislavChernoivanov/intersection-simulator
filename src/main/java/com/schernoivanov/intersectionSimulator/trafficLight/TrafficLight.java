package com.schernoivanov.intersectionSimulator.trafficLight;

import com.schernoivanov.intersectionSimulator.event.Event;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class TrafficLight {

    private Integer id;

    private int oppositeTrafficLightId;

    private int[] perpendicularTrafficLightIds;

    protected TrafficLightType trafficLightType;

    private RoadNumber roadNumber;

    private TrafficLightColor color;

    private BlockingQueue<Event<?>> eventQueue;

    private int queueSize;

    private final ScheduledExecutorService scheduler;

    private StopWatch stopWatch;

    private short trafficLightColorDuration;

    private int currentlyQueueSize;

    private String timer;


    public TrafficLight() {

        scheduler =
                Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

        Thread thread = new Thread(() -> {

            while (true) {

                try {
                    TimeUnit.MILLISECONDS.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                currentlyQueueSize = queueSize;

                while (color != null && color.equals(TrafficLightColor.GREEN)) {

                    try {
                        TimeUnit.SECONDS.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (currentlyQueueSize > 0) {
                        currentlyQueueSize--;
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
