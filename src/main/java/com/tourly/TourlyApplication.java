package com.tourly;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.tourly.trip.entity.Destination;
import com.tourly.trip.repository.DestinationRepository;

@SpringBootApplication
@EnableScheduling
public class TourlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(TourlyApplication.class, args);
	}

	@Bean
	public CommandLineRunner seedDatabase(DestinationRepository destinationRepository, com.tourly.auth.repository.RoleRepository roleRepository) {
		return args -> {
			// 1️⃣ Seed User Roles
			if (roleRepository.count() == 0) {
				System.out.println("Seeding default roles in the database...");
				createRole(roleRepository, com.tourly.auth.entity.RoleName.TRAVELER, "Standard Traveler Role");
				createRole(roleRepository, com.tourly.auth.entity.RoleName.PLANNER, "Trip Planner Role");
				createRole(roleRepository, com.tourly.auth.entity.RoleName.HOST, "Trip Host Role");
				createRole(roleRepository, com.tourly.auth.entity.RoleName.ADMIN, "System Administrator Role");
				System.out.println("Default roles successfully seeded!");
			}

			// 2️⃣ Seed Travel Destinations
			if (destinationRepository.count() == 0) {
				System.out.println("Seeding default destinations in the database...");
				
				createDestination(destinationRepository, "India", "Kerala", "Munnar", 10.0889, 77.0595);
				createDestination(destinationRepository, "India", "Goa", "Panaji", 15.4909, 73.8278);
				createDestination(destinationRepository, "India", "Rajasthan", "Udaipur", 24.5854, 73.7125);
				createDestination(destinationRepository, "India", "Rajasthan", "Jaipur", 26.9124, 75.7873);
				createDestination(destinationRepository, "India", "Karnataka", "Coorg", 12.3375, 75.8069);
				createDestination(destinationRepository, "India", "Karnataka", "Hampi", 15.3350, 76.4600);
				createDestination(destinationRepository, "India", "Uttar Pradesh", "Varanasi", 25.3176, 82.9739);
				createDestination(destinationRepository, "India", "Himachal Pradesh", "Shimla", 31.1048, 77.1734);
				createDestination(destinationRepository, "India", "Himachal Pradesh", "Manali", 32.2396, 77.1887);
				createDestination(destinationRepository, "India", "Ladakh", "Leh", 34.1526, 77.5771);
				
				System.out.println("Default destinations successfully seeded!");
			}
		};
	}

	private void createRole(com.tourly.auth.repository.RoleRepository repository, com.tourly.auth.entity.RoleName name, String description) {
		com.tourly.auth.entity.Role role = new com.tourly.auth.entity.Role();
		role.setName(name);
		role.setDescription(description);
		role.setIsActive(true);
		repository.save(role);
	}

	private void createDestination(DestinationRepository repository, String country, String state, String city, double lat, double lon) {
		Destination destination = new Destination();
		destination.setCountry(country);
		destination.setState(state);
		destination.setCity(city);
		destination.setLatitude(lat);
		destination.setLongitude(lon);
		repository.save(destination);
	}
}


