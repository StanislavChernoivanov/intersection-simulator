package com.schernoivanov.intersectionSimulator.dto;

import com.schernoivanov.intersectionSimulator.trafficLight.TrafficLight;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrafficLightMapper {


    TrafficLightDTO trafficLightToDTO(TrafficLight trafficLight);



    List<TrafficLightDTO> trafficLightListToDTOList(List<TrafficLight> trafficLights);



    default TrafficLightListDTO trafficLightListToTrafficLightDTOList(List<TrafficLight> trafficLights) {
        TrafficLightListDTO trafficLightListDTO = new TrafficLightListDTO();
        trafficLightListDTO.setTrafficLights(trafficLightListToDTOList(trafficLights));
        return trafficLightListDTO;


    }


}
