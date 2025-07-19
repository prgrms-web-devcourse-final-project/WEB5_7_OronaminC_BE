package com.oronaminc.join.global.dev;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "개발용 API")
public class HealthController {

    @Operation(summary = "애플리케이션 헬스체크")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/health")
    public String health() {
        return "Server is Healthy!";
    }

    @Operation(summary = "홈 헬스체크")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/")
    public String home() {
        return "It's Home!";
    }
}
