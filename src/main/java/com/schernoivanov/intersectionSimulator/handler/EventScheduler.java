package com.schernoivanov.intersectionSimulator.handler;

import com.schernoivanov.intersectionSimulator.event.ChangeTrafficLightColorEvent;
import com.schernoivanov.intersectionSimulator.event.DisableTrafficLightEvent;
import com.schernoivanov.intersectionSimulator.event.Event;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLight;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLightColor;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLightType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
                trafficLightSender.getScheduler();

        trafficLights.values().forEach(tl -> new Thread(() -> {

                    Event<String> event = new DisableTrafficLightEvent(
                            trafficLightSender.getId(),
                            tl.getId(),
                            TrafficLightColor.DISABLED.name());

                    SchedulingUtils.scheduleDisablingTrafficLightEventSending(tl,
                            delay,
                            scheduledThreadPool,
                            event);

                }).start());
    }





    public void scheduleSendEventForEnablingAllTrafficLights(TrafficLight trafficLightSender,
                                                             TrafficLight trafficLightReceiver,
                                                             TrafficLightColor scheduledColor,
                                                             boolean isFirstEnabling,
                                                             long delay) {

        boolean isEven = trafficLightSender.getId() % 2 == 0;

        String logInfoSchedulingTrafficLightEnabling =
                "Запланировано включение {} сигнала светофора " +
                        "(id={}, type={}, road_number={}) " +
                        "через {} секунды...";

        ScheduledExecutorService scheduler =
                trafficLightSender.getScheduler();

        if(!isFirstEnabling) { // Является ли включение светофора первым

            Event<String> event = new ChangeTrafficLightColorEvent(
                        trafficLightSender.getId(),
                        trafficLightReceiver.getId(),
                        scheduledColor.name()
                );

                SchedulingUtils.scheduleEnablingTrafficLightEventSending(logInfoSchedulingTrafficLightEnabling, delay,
                        trafficLightReceiver, scheduledColor, scheduler, event);

        } else {

            boolean isCompatible = trafficLightSender.getId() % 2 == trafficLightReceiver.getId() % 2;
            boolean isSameType = trafficLightSender.getTrafficLightType().equals(trafficLightReceiver.getTrafficLightType());
            boolean isGreen = scheduledColor.equals(TrafficLightColor.GREEN);
            TrafficLightColor anotherScheduledColor = isGreen ? TrafficLightColor.RED : TrafficLightColor.GREEN;

            if ((isCompatible && isSameType) || (!isCompatible && !isSameType)) {

                Event<String> event = new ChangeTrafficLightColorEvent(
                        trafficLightSender.getId(),
                        trafficLightReceiver.getId(),
                        scheduledColor.name()
                );

                SchedulingUtils.scheduleEnablingTrafficLightEventSending(logInfoSchedulingTrafficLightEnabling, delay,
                        trafficLightReceiver, scheduledColor, scheduler, event);
            } else {

                Event<String> event = new ChangeTrafficLightColorEvent(
                        trafficLightSender.getId(),
                        trafficLightReceiver.getId(),
                        anotherScheduledColor.name()
                );

                SchedulingUtils.scheduleEnablingTrafficLightEventSending(logInfoSchedulingTrafficLightEnabling, delay,
                        trafficLightReceiver, anotherScheduledColor, scheduler, event);
            }
        }
    }




    public void lightingControlScheduling(TrafficLight commonTrafficLight, TrafficLightColor color) {

        scheduleSendEventForDisablingAllTrafficLights(commonTrafficLight, 1);

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
        }
        List<Thread> threads = new ArrayList<>();
        trafficLights.values().forEach(tl -> {

            Thread thread = new Thread( () -> {
                scheduleSendEventForEnablingAllTrafficLights(commonTrafficLight,
                        tl,
                        color,
                        true,
                        5);

                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    log.warn(Arrays.toString(e.getStackTrace()));
                }
            });
            thread.start();
            threads.add(thread);
        });

        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                log.info(Arrays.toString(e.getStackTrace()));
            }
        });

        new Thread( () -> startTrafficLightsWorking(commonTrafficLight)).start();
    }





    public void startTrafficLightsWorking(TrafficLight commonTrafficLight) {

        trafficLights.values().forEach(tl -> asyncTrafficLightStateChanging(commonTrafficLight, tl));
    }





    private void asyncTrafficLightStateChanging(TrafficLight sender, TrafficLight receiver) {

        new Thread( () -> {

            while (isWork) {
                Thread thread = new Thread(() -> {

                    TrafficLightColor currentlyColor = receiver.getColor();
                    switch (currentlyColor) {

                        case GREEN:

                            if (receiver.getTrafficLightType().equals(TrafficLightType.VEHICLE)) {

                                scheduleSendEventForEnablingAllTrafficLights(
                                        sender,
                                        receiver,
                                        TrafficLightColor.YELLOW,
                                        false,
                                        greenColorDuration);

                                try {
                                    TimeUnit.SECONDS.sleep(greenColorDuration);
                                } catch (InterruptedException e) {

                                    log.info(Arrays.toString(e.getStackTrace()));
                                }
                            } else {

                                scheduleSendEventForEnablingAllTrafficLights(
                                        sender,
                                        receiver,
                                        TrafficLightColor.RED,
                                        false,
                                        greenColorDuration);

                                try {
                                    TimeUnit.SECONDS.sleep(greenColorDuration);
                                } catch (InterruptedException e) {

                                    log.info(Arrays.toString(e.getStackTrace()));
                                }
                            }
                            break;

                        case RED:

                            scheduleSendEventForEnablingAllTrafficLights(
                                    sender,
                                    receiver,
                                    TrafficLightColor.GREEN,
                                    false,
                                    redColorDuration);

                            try {
                                TimeUnit.SECONDS.sleep(redColorDuration);
                            } catch (InterruptedException e) {

                                log.info(Arrays.toString(e.getStackTrace()));
                            }
                            break;

                        case YELLOW:

                            scheduleSendEventForEnablingAllTrafficLights(
                                    sender,
                                    receiver,
                                    TrafficLightColor.RED,
                                    false,
                                    yellowColorDuration);

                            try {
                                TimeUnit.SECONDS.sleep(yellowColorDuration);
                            } catch (InterruptedException e) {

                                log.info(Arrays.toString(e.getStackTrace()));
                            }
                            break;
                    }
                }
                );
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    log.info(Arrays.toString(e.getStackTrace()));
                }
            }

        }).start();
    }





}
