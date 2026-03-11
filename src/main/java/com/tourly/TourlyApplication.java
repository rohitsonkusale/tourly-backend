package com.tourly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TourlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(TourlyApplication.class, args);
	}

}
