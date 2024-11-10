package com.schernoivanov.intersectionSimulator.event;

import lombok.Getter;

@Getter
public class ChangeQueueSizeEvent extends Event<Integer> {

    public ChangeQueueSizeEvent(Integer trafficLightFromId,
                                Integer trafficLightToId,
                                Integer eventData) {
        super(trafficLightFromId, trafficLightToId, eventData);


        setEventType(EventType.CHANGE_QUEUE_SIZE);
    }
}
