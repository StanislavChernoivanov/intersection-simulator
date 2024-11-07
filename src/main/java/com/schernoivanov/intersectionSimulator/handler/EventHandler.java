package com.schernoivanov.intersectionSimulator.handler;

import com.schernoivanov.intersectionSimulator.event.ChangeQueueSizeEvent;
import com.schernoivanov.intersectionSimulator.event.Event;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLight;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLightColor;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLightType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventHandler {

    private final Map<Integer, TrafficLight> trafficLights;


    @Value("${app.traffic-light.default-green-color-duration}")
    private int greenColorDuration;
    @Value("${app.traffic-light.default-red-color-duration}")
    private int redColorDuration;
    @Value("${app.traffic-light.default-yellow-color-duration}")
    private int yellowColorDuration;


    public void handleEvent(TrafficLight trafficLight, Event<?> event) {

        switch (event.getEventType()) {
            case DISABLE:
                handleDisableEvent(trafficLight, event);
                break;

            case CHANGE_COLOR:
                handleChangeColorEvent(trafficLight, event);
                break;

            case CHANGE_QUEUE_SIZE:
                handleChangeQueueSize(trafficLight, event);
        }
    }

    private void handleDisableEvent(TrafficLight trafficLight, Event<?> event) {

        log.info(
                "Cветофор (id={}, type={}, road_number={}) отключен",
                trafficLight.getId(),
                trafficLight.getTrafficLightType(),
                trafficLight.getRoadNumber()
        );

        trafficLight.setColor(TrafficLightColor.DISABLED);
        trafficLight.setStopWatch(StopWatch.createStarted());
        trafficLight.getEventQueue().clear();
    }

    private void handleChangeColorEvent(TrafficLight trafficLight, Event<?> event) {

        String eventData = (String) event.getEventData();

        TrafficLightColor newColor = TrafficLightColor.valueOf(eventData.toUpperCase());

        switch (newColor) {

            case RED:
                trafficLight.setColor(TrafficLightColor.RED);
                trafficLight.setStopWatch(StopWatch.createStarted());

                log.info("Состояние светофора (id={}, type={}, road_number={})" +
                                " - {} - движение запрещено",
                        trafficLight.getId(),
                        trafficLight.getTrafficLightType(),
                        trafficLight.getRoadNumber(),
                        TrafficLightColor.RED);

                break;

            case GREEN:
                trafficLight.setColor(TrafficLightColor.GREEN);
                trafficLight.setStopWatch(StopWatch.createStarted());

                log.info("Состояние светофора (id={}, type={}, road_number={}) - " +
                                "{} - движение разрешено",
                        trafficLight.getId(),
                        trafficLight.getTrafficLightType(),
                        trafficLight.getRoadNumber(),
                        TrafficLightColor.GREEN);
                break;

            case YELLOW:
                trafficLight.setColor(TrafficLightColor.YELLOW);
                trafficLight.setStopWatch(StopWatch.createStarted());

                log.info("Состояние светофора (id={}, type={}, road_number={}) " +
                                "- {} - через {} секунд движение будет запрещено",
                        trafficLight.getId(),
                        trafficLight.getTrafficLightType(),
                        trafficLight.getRoadNumber(),
                        TrafficLightColor.YELLOW,
                        yellowColorDuration);
                break;
        }

    }



    private void handleChangeQueueSize(TrafficLight trafficLight, Event<?> event) {
        if (trafficLight.getTrafficLightType().equals(TrafficLightType.VEHICLE)) {

            trafficLight.setQueueSize(((ChangeQueueSizeEvent) event).getNewQueueSize());

            log.info("Очередь объектов (машин или людей) более чем в 2 раза, " +
                            "необходимо увеличить продолжительность зеленого сигнала светофора " +
                            "(id={}, type={}, road_number={}, queue_size={})",
                    trafficLight.getId(),
                    trafficLight.getTrafficLightType(),
                    trafficLight.getRoadNumber(),
                    trafficLight.getQueueSize());

//            if(trafficLight.getQueueSize() * 2 < ((ChangeQueueSizeEvent) event).getNewQueueSize()
//                && trafficLight.getQueueSize() * 3 > ((ChangeQueueSizeEvent) event).getNewQueueSize()) {
//
//
//
//            } else if(trafficLight.getQueueSize() * 3 < ((ChangeQueueSizeEvent) event).getNewQueueSize()
//                    && trafficLight.getQueueSize() * 4 > ((ChangeQueueSizeEvent) event).getNewQueueSize()) {
//
//
//
//            } else if(trafficLight.getQueueSize() * 4 < ((ChangeQueueSizeEvent) event).getNewQueueSize()
//                    && trafficLight.getQueueSize() * 5 > ((ChangeQueueSizeEvent) event).getNewQueueSize()) {
//
//
//
//            }
        }
    }
}
