package com.bureureung.fo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class FoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoApplication.class, args);
    }

}
