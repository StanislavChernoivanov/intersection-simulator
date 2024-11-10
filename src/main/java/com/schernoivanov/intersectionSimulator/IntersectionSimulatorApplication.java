package com.schernoivanov.intersectionSimulator;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;


@SpringBootApplication
@Slf4j
public class IntersectionSimulatorApplication {

	public static void main(String[] args) {

		SpringApplication.run(IntersectionSimulatorApplication.class, args);

	}

}
