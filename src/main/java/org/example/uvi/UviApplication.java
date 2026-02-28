package org.example.uvi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UviApplication {

    public static void main(String[] args) {
        SpringApplication.run(UviApplication.class, args);
    }

}
