package com.likelion.team4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Team4Application {

	public static void main(String[] args) {
		SpringApplication.run(Team4Application.class, args);
	}

}
