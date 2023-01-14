package com.example.birdnest_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BirdnestBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(BirdnestBackApplication.class, args);
    }

}
