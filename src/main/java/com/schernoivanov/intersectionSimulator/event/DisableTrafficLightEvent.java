package com.schernoivanov.intersectionSimulator.event;

public class DisableTrafficLightEvent extends Event<String> {

    public DisableTrafficLightEvent(Integer trafficLightFromId, Integer trafficLightToId, String eventData) {

        super(trafficLightFromId, trafficLightToId, eventData);

        setEventType(EventType.DISABLE);
    }
}
