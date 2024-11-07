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

	@PostConstruct
	public void timer() {
		new Thread( () -> {
			for (int i =0; i < 10_000; i++) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				System.out.println(i);
			}
		}).start();
	}

}
