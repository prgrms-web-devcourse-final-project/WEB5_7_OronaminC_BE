package com.oronaminc.join.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorException extends RuntimeException {

    private final ErrorCode errorCode;

}
