package com.schernoivanov.intersectionSimulator.controller;

import com.schernoivanov.intersectionSimulator.dto.TrafficLightDTO;
import com.schernoivanov.intersectionSimulator.dto.TrafficLightListDTO;
import com.schernoivanov.intersectionSimulator.dto.TrafficLightMapper;
import com.schernoivanov.intersectionSimulator.service.IntersectionService;
import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLight;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/intersection")
@RequiredArgsConstructor
public class IntersectionController {

    private final IntersectionService intersectionService;

    private final TrafficLightMapper trafficLightMapper;

    @GetMapping
    public ResponseEntity<TrafficLightListDTO> getAllTrafficLights() {

        List<TrafficLight> trafficLights = intersectionService.getAllTrafficLight();
        return ResponseEntity.ok(
                trafficLightMapper.trafficLightListToTrafficLightDTOList(
                        intersectionService.getAllTrafficLight()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrafficLightDTO> getTrafficLightById(@PathVariable Integer id) {

        return ResponseEntity.ok(trafficLightMapper.trafficLightToDTO(
                intersectionService.getTrafficLightById(id)
        ));
    }

    @PutMapping("/changeTrafficLightColor/{id}")
    public ResponseEntity<TrafficLightDTO> changeTrafficLightColorById(@PathVariable Integer id,
                                                                       @RequestParam String newColor) throws InterruptedException {

        intersectionService.changeTrafficLightColorById(id, newColor.toUpperCase());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @PutMapping("/queueSize/{id}")
    public ResponseEntity<TrafficLightDTO> changeQueueSizeById(@PathVariable Integer id,
                                                               @RequestParam int queueSize) {

        TrafficLight trafficLight = intersectionService.changeQueueSizeById(id, queueSize);

        return ResponseEntity.ok(trafficLightMapper.trafficLightToDTO(
                trafficLight
        ));
    }

}
