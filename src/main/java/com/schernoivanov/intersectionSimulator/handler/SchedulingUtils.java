package com.schernoivanov.intersectionSimulator.handler;

import com.schernoivanov.intersectionSimulator.event.Event;
import com.schernoivanov.intersectionSimulator.trafficLight.RoadNumber;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLight;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLightColor;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLightType;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@UtilityClass
@Slf4j
public class SchedulingUtils {



    public void scheduleEnablingTrafficLightEventSending(String logInfoSchedulingTrafficLightEnabling,
                                                         long duration,
                                                         TrafficLight sender,
                                                         TrafficLight receiver,
                                                         TrafficLightColor scheduledColor,
                                                         ScheduledExecutorService scheduler,
                                                         Event<String> event) {

        log.debug(logInfoSchedulingTrafficLightEnabling,
                scheduledColor,
                receiver.getId(),
                receiver.getTrafficLightType(),
                receiver.getRoadNumber(),
                duration);

        scheduler.schedule(
                () -> sender.sendEvent(event, receiver),
                duration,
                TimeUnit.SECONDS
        );
    }

    public void scheduleDisablingTrafficLightEventSending(TrafficLight sender,
                                                          TrafficLight receiver,
                                                          long delay,
                                                          ScheduledExecutorService scheduler,
                                                          Event<String> event) {

        log.info(
                "Запланировано отключение светофора (id={}, type={}, road_number={}) " +
                        "через {} секунды...",
                receiver.getId(),
                receiver.getTrafficLightType(),
                receiver.getRoadNumber(),
                delay);

        scheduler.schedule(
                () -> sender.sendEvent(event, receiver),
                delay,
                TimeUnit.SECONDS
        );
    }



}
