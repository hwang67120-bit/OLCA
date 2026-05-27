package com.example.olca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EnableReactiveMongoRepositories
public class OlcaApplication {

	public static void main(String[] args) {
		SpringApplication.run(OlcaApplication.class, args);
	}

}
