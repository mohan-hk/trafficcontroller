package com.natwest.tc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrafficcontrollerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrafficcontrollerApplication.class, args);
	}

}
