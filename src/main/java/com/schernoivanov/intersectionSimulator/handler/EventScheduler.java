package com.schernoivanov.intersectionSimulator.handler;

import com.schernoivanov.intersectionSimulator.event.ChangeTrafficLightColorEvent;
import com.schernoivanov.intersectionSimulator.event.DisableTrafficLightEvent;
import com.schernoivanov.intersectionSimulator.event.Event;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLight;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLightColor;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLightType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


    private final Map<String, Integer> trafficLightColorDurations;

    public static boolean isWork = true;


    public void scheduleSendEventForDisablingAllTrafficLights(TrafficLight trafficLightSender,
                                                              long delay) {

        ScheduledExecutorService scheduledThreadPool =
                trafficLightSender.getScheduler();

        List<Thread> threads = new ArrayList<>();
        trafficLights.values().forEach(tl -> {
            Thread thread = new Thread(() -> {

                Event<String> event = new DisableTrafficLightEvent(
                        trafficLightSender.getId(),
                        tl.getId(),
                        TrafficLightColor.DISABLED.name());

                SchedulingUtils.scheduleDisablingTrafficLightEventSending(trafficLightSender,
                        tl,
                        delay,
                        scheduledThreadPool,
                        event);

            });
            thread.start();
            threads.add(thread);
        });

        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                log.warn(Arrays.toString(e.getStackTrace()));
            }
        });
    }


    public void scheduleSendEventForEnablingAllTrafficLights(TrafficLight trafficLightSender,
                                                             TrafficLight trafficLightReceiver,
                                                             TrafficLightColor scheduledColor,
                                                             boolean isFirstEnabling,
                                                             long delay) {

        String logInfoSchedulingTrafficLightEnabling =
                "Запланировано включение {} сигнала светофора " +
                        "(id={}, type={}, road_number={}) " +
                        "через {} секунды...";

        ScheduledExecutorService scheduler =
                trafficLightSender.getScheduler();

        if (!isFirstEnabling) { // Является ли включение светофора первым

            Event<String> event = new ChangeTrafficLightColorEvent(
                    trafficLightSender.getId(),
                    trafficLightReceiver.getId(),
                    scheduledColor.name()
            );

            SchedulingUtils.scheduleEnablingTrafficLightEventSending(logInfoSchedulingTrafficLightEnabling, delay,
                    trafficLightSender, trafficLightReceiver, scheduledColor, scheduler, event);

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
                        trafficLightSender, trafficLightReceiver, scheduledColor, scheduler, event);
            } else {

                Event<String> event = new ChangeTrafficLightColorEvent(
                        trafficLightSender.getId(),
                        trafficLightReceiver.getId(),
                        anotherScheduledColor.name()
                );

                SchedulingUtils.scheduleEnablingTrafficLightEventSending(logInfoSchedulingTrafficLightEnabling, delay,
                        trafficLightSender, trafficLightReceiver, anotherScheduledColor, scheduler, event);
            }
        }
    }


    public void lightingControlScheduling(TrafficLight commonTrafficLight, TrafficLightColor color) throws InterruptedException {

        isWork = false;
        TimeUnit.SECONDS.sleep(1);

        scheduleSendEventForDisablingAllTrafficLights(commonTrafficLight, 1);

        List<Thread> threads = new ArrayList<>();
        trafficLights.values().forEach(tl -> {

            Thread thread = new Thread(() ->
                    scheduleSendEventForEnablingAllTrafficLights(
                            commonTrafficLight,
                            tl,
                            color,
                            true,
                            5));
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

        isWork = true;

        new Thread(() -> startTrafficLightsWorking(commonTrafficLight)).start();
    }


    public void startTrafficLightsWorking(TrafficLight commonTrafficLight) {

        trafficLights.values().forEach(tl -> asyncTrafficLightStateChanging(commonTrafficLight, tl));
    }


    private void asyncTrafficLightStateChanging(TrafficLight sender, TrafficLight receiver) {

        new Thread(() -> {

            while (isWork) {
                Thread thread = new Thread(() -> {

                    TrafficLightColor currentlyColor = receiver.getColor();
                    int duration = 0;

                    if (currentlyColor.equals(TrafficLightColor.RED)) {

                        duration = receiver.getId() % 2 == 0 && receiver.getTrafficLightType().equals(TrafficLightType.VEHICLE) ?
                                trafficLightColorDurations.get("red24") : receiver.getId() % 2 == 0 ?
                                trafficLightColorDurations.get("red681012") :
                                receiver.getTrafficLightType().equals(TrafficLightType.VEHICLE) ?
                                        trafficLightColorDurations.get("red13") : trafficLightColorDurations.get("red57911");
                    } else if (currentlyColor.equals(TrafficLightColor.GREEN)) {

                        duration = receiver.getId() % 2 == 0
                                ? trafficLightColorDurations.get("green24681012")
                                : trafficLightColorDurations.get("green1357911");

                    }

                    receiver.getStopWatch().stop();
                    receiver.getStopWatch().reset();
                    receiver.getStopWatch().start();
                    switch (currentlyColor) {

                        case GREEN:
                            receiver.setTrafficLightColorDuration((short) duration);

                            if (receiver.getTrafficLightType().equals(TrafficLightType.VEHICLE)) {

                                scheduleSendEventForEnablingAllTrafficLights(
                                        sender,
                                        receiver,
                                        TrafficLightColor.YELLOW,
                                        false,
                                        duration);

                                try {
                                    TimeUnit.SECONDS.sleep(duration);
                                } catch (InterruptedException e) {

                                    log.info(Arrays.toString(e.getStackTrace()));
                                }
                            } else {

                                scheduleSendEventForEnablingAllTrafficLights(
                                        sender,
                                        receiver,
                                        TrafficLightColor.RED,
                                        false,
                                        duration);

                                try {
                                    TimeUnit.SECONDS.sleep(duration);
                                } catch (InterruptedException e) {

                                    log.info(Arrays.toString(e.getStackTrace()));
                                }
                            }
                            break;

                        case RED:
                            receiver.setTrafficLightColorDuration((short) duration);

                            scheduleSendEventForEnablingAllTrafficLights(
                                    sender,
                                    receiver,
                                    TrafficLightColor.GREEN,
                                    false,
                                    duration);

                            try {
                                TimeUnit.SECONDS.sleep(duration);
                            } catch (InterruptedException e) {

                                log.info(Arrays.toString(e.getStackTrace()));
                            }
                            break;

                        case YELLOW:
                            receiver.setTrafficLightColorDuration(trafficLightColorDurations.get("yellow").shortValue());

                            scheduleSendEventForEnablingAllTrafficLights(
                                    sender,
                                    receiver,
                                    TrafficLightColor.RED,
                                    false,
                                    trafficLightColorDurations.get("yellow"));

                            try {
                                TimeUnit.SECONDS.sleep(trafficLightColorDurations.get("yellow"));
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
