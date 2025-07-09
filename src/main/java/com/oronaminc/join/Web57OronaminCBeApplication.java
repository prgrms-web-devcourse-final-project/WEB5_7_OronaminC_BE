package com.oronaminc.join;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Web57OronaminCBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(Web57OronaminCBeApplication.class, args);
    }

}
