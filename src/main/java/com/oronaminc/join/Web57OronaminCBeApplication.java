package com.oronaminc.join;

import com.oronaminc.join.member.token.JwtConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties(JwtConfiguration.class)
public class Web57OronaminCBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(Web57OronaminCBeApplication.class, args);
    }

}
