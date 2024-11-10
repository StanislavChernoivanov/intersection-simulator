package com.schernoivanov.intersectionSimulator.event;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public abstract class Event<T> {

    private final Integer trafficLightFromId;

    @Setter
    private Integer trafficLightToId;

    @Setter
    private T eventData;

    @Setter
    protected EventType eventType;


    public Event(Integer trafficLightFromId, Integer trafficLightToId, T eventData) {


        this.trafficLightFromId = trafficLightFromId;
        this.trafficLightToId = trafficLightToId;
        this.eventData = eventData;
    }

}
