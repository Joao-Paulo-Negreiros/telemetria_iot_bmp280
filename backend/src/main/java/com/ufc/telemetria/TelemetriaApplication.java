package com.ufc.telemetria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TelemetriaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelemetriaApplication.class, args);
    }
}
