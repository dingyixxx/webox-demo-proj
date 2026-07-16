package com.example.weboxbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WeboxBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeboxBackendApplication.class, args);
    }

}
