package com.oronaminc.join.global.exception;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class ExceptionTestController {

    @GetMapping("/error")
    public void throwError() {
        throw new ErrorException(ErrorCode.NOT_FOUND_MEMBER);
    }

}
