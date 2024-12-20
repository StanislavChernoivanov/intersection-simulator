package com.schernoivanov.intersectionSimulator.event;

import lombok.Getter;

@Getter
public class ChangeTrafficLightColorEvent extends Event<String> {


    public ChangeTrafficLightColorEvent(Integer trafficLightFromId,
                                        Integer trafficLightToId,
                                        String eventData) {

        super(trafficLightFromId, trafficLightToId, eventData);

        setEventType(EventType.CHANGE_COLOR);
    }
}
