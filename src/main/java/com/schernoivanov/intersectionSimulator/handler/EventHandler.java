package com.schernoivanov.intersectionSimulator.handler;

import com.schernoivanov.intersectionSimulator.event.Event;
import com.schernoivanov.intersectionSimulator.trafficLight.PedestrianTrafficLight;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLight;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLightColor;
import com.schernoivanov.intersectionSimulator.trafficLight.VehicleTrafficLight;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventHandler {

    private final Map<String, Integer> trafficLightColorDurations;

    private final Map<Integer, TrafficLight> trafficLights;

    @Value("${app.traffic-light.default-green-color-duration}")
    private int greenColorDuration;
    @Value("${app.traffic-light.default-red-color-duration}")
    private int redColorDuration;
    @Value("${app.traffic-light.default-yellow-color-duration}")
    private int yellowColorDuration;

    public final AtomicInteger changingCount = new AtomicInteger();


    public void handleEvent(TrafficLight trafficLight, Event<?> event) {

        switch (event.getEventType()) {
            case DISABLE:
                handleDisableEvent(trafficLight);
                break;

            case CHANGE_COLOR:
                handleChangeColorEvent(trafficLight, event);
                break;

            case CHANGE_QUEUE_SIZE:
                handleChangeQueueSize(event);
        }
    }


    private void handleDisableEvent(TrafficLight trafficLight) {

        trafficLight.getEventQueue().clear();

        log.info(
                "Cветофор (id={}, type={}, road_number={}) отключен",
                trafficLight.getId(),
                trafficLight.getTrafficLightType(),
                trafficLight.getRoadNumber()
        );

        trafficLight.setColor(TrafficLightColor.DISABLED);
        trafficLight.setStopWatch(StopWatch.createStarted());
    }


    private void handleChangeColorEvent(TrafficLight trafficLight, Event<?> event) {

        String eventData = (String) event.getEventData();

        TrafficLightColor newColor = TrafficLightColor.valueOf(eventData.toUpperCase());

        switch (newColor) {

            case RED:
                trafficLight.setColor(TrafficLightColor.RED);

                log.info("Состояние светофора (id={}, type={}, road_number={})" +
                                " - {} - движение запрещено",
                        trafficLight.getId(),
                        trafficLight.getTrafficLightType(),
                        trafficLight.getRoadNumber(),
                        TrafficLightColor.RED);

                break;

            case GREEN:
                trafficLight.setColor(TrafficLightColor.GREEN);

                log.info("Состояние светофора (id={}, type={}, road_number={}) - " +
                                "{} - движение разрешено",
                        trafficLight.getId(),
                        trafficLight.getTrafficLightType(),
                        trafficLight.getRoadNumber(),
                        TrafficLightColor.GREEN);
                break;

            case YELLOW:
                trafficLight.setColor(TrafficLightColor.YELLOW);

                log.info("Состояние светофора (id={}, type={}, road_number={}) " +
                                "- {} - через {} секунд движение будет запрещено",
                        trafficLight.getId(),
                        trafficLight.getTrafficLightType(),
                        trafficLight.getRoadNumber(),
                        TrafficLightColor.YELLOW,
                        trafficLightColorDurations.get("yellow"));
                break;
        }

    }


    private void handleChangeQueueSize(Event<?> event) {

        TrafficLight sender = trafficLights.get(event.getTrafficLightFromId());
        TrafficLight receiver = trafficLights.get(event.getTrafficLightToId());

        int newQueueSize = (int) event.getEventData();
        int currentlyQueueSize = receiver.getQueueSize();

        if (sender instanceof VehicleTrafficLight) {

            changeTrafficLightGreenColorDuration(receiver, newQueueSize, currentlyQueueSize);
            changeTrafficLightGreenColorDuration(
                    trafficLights.get(receiver.getOppositeTrafficLightId())
                    , newQueueSize
                    , currentlyQueueSize);

        } else {

            changeTrafficLightGreenColorDuration(receiver, newQueueSize, currentlyQueueSize);
            changeTrafficLightGreenColorDuration(
                    trafficLights.get(receiver.getOppositeTrafficLightId())
                    , newQueueSize
                    , currentlyQueueSize);
            changeTrafficLightGreenColorDuration(
                    trafficLights.get(((PedestrianTrafficLight) receiver).getParallelTrafficLightId())
                    , newQueueSize
                    , currentlyQueueSize);
        }
    }


    private void changeTrafficLightGreenColorDuration(TrafficLight receiver,
                                                      int newQueueSize,
                                                      int currentlyQueueSize) {

        int multiplicity = getMultiplicityBetweenQueueSizes(newQueueSize, currentlyQueueSize);

        receiver.setQueueSize(newQueueSize);

        int currentlyDuration;
        int updatedDuration;

        Optional<String> key = findKey(receiver);

        if (multiplicity > 1) {

            log.debug("\nОчередь объектов (машин или людей) увеличилась более чем в {} раза, " +
                            "необходимо увеличить продолжительность зеленого сигнала светофора " +
                            "(id={}, type={}, road_number={}, queue_size={})\n",
                    multiplicity,
                    receiver.getId(),
                    receiver.getTrafficLightType(),
                    receiver.getRoadNumber(),
                    receiver.getQueueSize());

            if (key.isPresent()) {

                currentlyDuration = trafficLightColorDurations.get(key.get());
                updatedDuration = currentlyDuration + 10 * (multiplicity - 1);

                trafficLightColorDurations.put(key.get(), changingCount.incrementAndGet() < 2 ?
                        updatedDuration : currentlyDuration);
            }
        } else if (multiplicity < -1) {

            log.debug("\nОчередь объектов (машин или людей) уменьшилась более чем в {} раза, " +
                            "необходимо уменьшить продолжительность зеленого сигнала светофора " +
                            "(id={}, type={}, road_number={}, queue_size={})\n",
                    multiplicity / -1,
                    receiver.getId(),
                    receiver.getTrafficLightType(),
                    receiver.getRoadNumber(),
                    receiver.getQueueSize());

            if (key.isPresent()) {

                currentlyDuration = trafficLightColorDurations.get(key.get());
                updatedDuration = currentlyDuration - 10 * (multiplicity - 1);


                trafficLightColorDurations.put(key.get(), changingCount.incrementAndGet() < 2 ?
                        Integer.max(currentlyDuration, updatedDuration) : currentlyDuration);

            }
        }
    }


    private Optional<String> findKey(TrafficLight receiver) {

        return trafficLightColorDurations.keySet().stream().filter(k -> {
            if (receiver.getId() == 1) {

                return StringUtils.countOccurrencesOf(k, String.valueOf(receiver.getId())) == 1
                        && StringUtils.countOccurrencesOf(k, String.valueOf(receiver.getId())) == 3;
            } else if (receiver.getId() == 2) {

                return StringUtils.countOccurrencesOf(k, String.valueOf(receiver.getId())) == 2;
            } else {

                return k.contains(String.valueOf(receiver.getId())) && k.contains("green");
            }
        }).findFirst();
    }


    private static synchronized int getMultiplicityBetweenQueueSizes(int newQueueSize, int currentlyQueueSize) {

        if (newQueueSize >= currentlyQueueSize) return newQueueSize / currentlyQueueSize;

        else return (-currentlyQueueSize) / newQueueSize;
    }


}
