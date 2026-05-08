package br.com.arenamatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ArenamatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArenamatchApplication.class, args);
    }

}
