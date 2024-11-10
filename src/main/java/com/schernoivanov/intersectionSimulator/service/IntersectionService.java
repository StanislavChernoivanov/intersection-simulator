package com.schernoivanov.intersectionSimulator.service;

import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLight;

import java.util.List;

public interface IntersectionService {

     List<TrafficLight> getAllTrafficLight();

     TrafficLight getTrafficLightById(Integer id);

     void changeTrafficLightColorById(Integer id, String newColor) throws InterruptedException;

     TrafficLight changeQueueSizeById(Integer id, int queueSize);



}
