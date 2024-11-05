package com.schernoivanov.intersectionSimulator.event;

import lombok.Getter;

@Getter
public class ChangeQueueSizeEvent extends Event<Integer> {

    private final int newQueueSize;

    public ChangeQueueSizeEvent(Integer trafficLightFromId,
                                Integer trafficLightToId,
                                Integer eventData,
                                int newQueueSize) {
        super(trafficLightFromId, trafficLightToId, eventData);

        this.newQueueSize = newQueueSize;

        setEventType(EventType.CHANGE_QUEUE_SIZE);
    }
}
