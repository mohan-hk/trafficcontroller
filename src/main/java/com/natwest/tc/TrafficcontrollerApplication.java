package com.natwest.tc;

import com.natwest.tc.service.TrafficEngineService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

@SpringBootApplication
@EnableScheduling
public class TrafficcontrollerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrafficcontrollerApplication.class, args);
	}
	@Bean
	public CommandLineRunner startupScript(TrafficEngineService trafficService) {
		return args -> {
			System.out.println("Starting Traffic Signal Engine...");
			// Start the default cycle (Phases 1, 2, 3, 4)
			trafficService.startCycle(Arrays.asList(1, 2, 3, 4));
		};
	}
}
