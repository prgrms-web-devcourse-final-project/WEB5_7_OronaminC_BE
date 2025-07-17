package com.oronaminc.join.global.exception;

import com.oronaminc.join.global.util.StringUtil;
import java.text.MessageFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String errorMessage;

    public ErrorException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public static ErrorException of(ErrorCode errorCode, String message, Object... args) {
        String errorMessage = createMessage(message, args);
        return new ErrorException(errorCode, errorMessage);
    }

    public String createMessage(String message, Object... args) {
        return StringUtil.format(message, args);
    }

}