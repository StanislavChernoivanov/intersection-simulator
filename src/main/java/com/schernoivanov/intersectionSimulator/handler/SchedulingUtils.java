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

    public long getDuration(boolean isCompatible,
                            TrafficLightColor scheduledColor,
                            long greenColorDuration,
                            long redColorDuration,
                            long yellowColorDuration) {

        if(isCompatible) {

            return scheduledColor.equals(TrafficLightColor.GREEN)
                    ? greenColorDuration : scheduledColor.equals(TrafficLightColor.RED)
                    ? redColorDuration
                    : yellowColorDuration;
        } else {

           return scheduledColor.equals(TrafficLightColor.GREEN)
                    ? redColorDuration : scheduledColor.equals(TrafficLightColor.RED)
                    ? greenColorDuration
                    : yellowColorDuration;
        }
    }

    public boolean isEvenRoad(String roadNumber) {

        return !roadNumber.equals(RoadNumber.FIRST.name());
    }


    public boolean isVehicleRoad(TrafficLightType type) {

        return type.equals(TrafficLightType.VEHICLE);
    }


}
