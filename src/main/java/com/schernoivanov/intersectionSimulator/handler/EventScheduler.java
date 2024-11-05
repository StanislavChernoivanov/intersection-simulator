package com.schernoivanov.intersectionSimulator.handler;

import com.schernoivanov.intersectionSimulator.event.ChangeTrafficLightColorEvent;
import com.schernoivanov.intersectionSimulator.event.DisableTrafficLightEvent;
import com.schernoivanov.intersectionSimulator.event.Event;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLight;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLightColor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventScheduler {


    private final Map<Integer, TrafficLight> trafficLights;

    private final EventsConsumerStarter eventsConsumerStarter;

    @Value("${app.traffic-light.default-green-color-duration}")
    private int greenColorDuration;
    @Value("${app.traffic-light.default-red-color-duration}")
    private int redColorDuration;
    @Value("${app.traffic-light.default-yellow-color-duration}")
    private int yellowColorDuration;

    public static boolean isWork = true;


    public void scheduleSendEventForDisablingAllTrafficLights(TrafficLight trafficLightSender,
                                                              long delay) {

        ScheduledExecutorService scheduledThreadPool =
                trafficLightSender.getScheduledThreadPool();

        trafficLights.values().stream().filter(tl -> !tl.getId().equals(trafficLightSender.getId()))
                .forEach(tl -> new Thread(() -> {

                    Event<String> event = new DisableTrafficLightEvent(
                            trafficLightSender.getId(),
                            tl.getId(),
                            TrafficLightColor.DISABLED.name());

                    log.info(
                            "Запланировано отключение светофора (id={}, type={}, road_number={}) " +
                                    "через {} секунды...",
                            tl.getId(),
                            tl.getTrafficLightType(),
                            tl.getRoadNumber(),
                            delay);

                    scheduledThreadPool.schedule(
                            () -> tl.addEvent(event),
                            delay,
                            TimeUnit.SECONDS
                    );
                }).start());
    }


    public void scheduleSendEventForEnablingAllTrafficLights(TrafficLight trafficLightSender,
                                                             TrafficLightColor scheduledColor,
                                                             long delay) {

        boolean isEven = trafficLightSender.getId() % 2 == 0;

        String logInfoSchedulingTrafficLightEnabling =
                "Запланировано включение {} сигнала светофора светофора " +
                        "(id={}, type={}, road_number={}) " +
                        "через {} секунды...";

        ScheduledExecutorService scheduledThreadPool =
                trafficLightSender.getScheduledThreadPool();

        trafficLights.values().stream().filter(tl -> !tl.getId().equals(trafficLightSender.getId())
                        && (tl.getId() % 2 == 0) == isEven)
                .forEach(tl -> new Thread(() -> {

                    Event<String> event = new ChangeTrafficLightColorEvent(
                            trafficLightSender.getId(),
                            tl.getId(),
                            scheduledColor.name()
                    );

                    log.info(logInfoSchedulingTrafficLightEnabling,
                            scheduledColor,
                            tl.getId(),
                            tl.getTrafficLightType(),
                            tl.getRoadNumber(),
                            delay);

                    scheduledThreadPool.schedule(
                            () -> tl.addEvent(event),
                            delay,
                            TimeUnit.SECONDS
                    );
                }).start());


        trafficLights.values().stream().filter(tl -> !tl.getId().equals(trafficLightSender.getId())
                        && (tl.getId() % 2 == 0) != isEven)
                .forEach(tl -> new Thread(() -> {

                    boolean isGreen = scheduledColor.equals(TrafficLightColor.GREEN);

                    Event<String> event = new ChangeTrafficLightColorEvent(
                            trafficLightSender.getId(),
                            tl.getId(),
                            isGreen ? TrafficLightColor.RED.name() : TrafficLightColor.GREEN.name()
                    );

                    log.info(logInfoSchedulingTrafficLightEnabling,
                            TrafficLightColor.RED,
                            tl.getId(),
                            tl.getTrafficLightType(),
                            tl.getRoadNumber(),
                            delay);

                    scheduledThreadPool.schedule(
                            () -> tl.addEvent(event),
                            3,
                            TimeUnit.SECONDS
                    );
                }).start());
    }

    public void lightingControlScheduling(TrafficLight commonTrafficLight, TrafficLightColor color) {

        Event<String> changeTrafficLightColorEvent = new ChangeTrafficLightColorEvent(
                commonTrafficLight.getId(),
                commonTrafficLight.getId(),
                color.name()
        );

        Event<String> disableTrafficLightEvent = new DisableTrafficLightEvent(
                commonTrafficLight.getId(),
                commonTrafficLight.getId(),
                TrafficLightColor.DISABLED.name());

        ScheduledExecutorService executorService = commonTrafficLight.getScheduledThreadPool();

        executorService.schedule(
                () -> commonTrafficLight.addEvent(disableTrafficLightEvent),
                1,
                TimeUnit.SECONDS);

        scheduleSendEventForDisablingAllTrafficLights(commonTrafficLight, 1);

        executorService.schedule(
                () -> commonTrafficLight.addEvent(changeTrafficLightColorEvent),
                1,
                TimeUnit.SECONDS);

        scheduleSendEventForEnablingAllTrafficLights(commonTrafficLight, color, 1);

        try {
            Thread.sleep(TimeUnit.SECONDS.toSeconds(1));
        } catch (InterruptedException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
        }

        new Thread(() -> {

            while (isWork) {
                if (commonTrafficLight.getColor().equals(TrafficLightColor.GREEN)) {
                    executorService.schedule(
                            () -> {
                                changeTrafficLightColorEvent.setEventData(TrafficLightColor.RED.name());
                                commonTrafficLight.addEvent(changeTrafficLightColorEvent);
                            },
                            redColorDuration,
                            TimeUnit.SECONDS);

                    scheduleSendEventForEnablingAllTrafficLights(
                            commonTrafficLight,
                            TrafficLightColor.RED,
                            redColorDuration);

                } else if (commonTrafficLight.getColor().equals(TrafficLightColor.RED)) {

                    executorService.schedule(
                            () -> {
                                changeTrafficLightColorEvent.setEventData(TrafficLightColor.GREEN.name());
                                commonTrafficLight.addEvent(changeTrafficLightColorEvent);
                            },
                            greenColorDuration,
                            TimeUnit.SECONDS);

                    scheduleSendEventForEnablingAllTrafficLights(
                            commonTrafficLight,
                            TrafficLightColor.GREEN,
                            greenColorDuration);
                }
            }

        }).start();

    }
}
