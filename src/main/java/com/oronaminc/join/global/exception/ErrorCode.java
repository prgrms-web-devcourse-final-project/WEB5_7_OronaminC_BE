package com.oronaminc.join.global.exception;

import static com.oronaminc.join.global.exception.ErrorStatus.INTERNAL_SERVER_ERROR;
import static com.oronaminc.join.global.exception.ErrorStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 상황에 따라 추가
    NOT_FOUND_MEMBER("AUTH-001", "존재하지 않는 회원입니다.", NOT_FOUND),
    NOT_FOUND_ROOM("ROOM-001", "존재하지 않는 발표방입니다.", NOT_FOUND),
    NOT_FOUND_PARTICIPANT("PARTICIPANT-001", "발표방에 존재하지 않는 회원입니다.", NOT_FOUND),
    FILE_UPLOAD_FAILED("FILE-001", "파일 업로드에 실패하였습니다.", INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final ErrorStatus errorStatus;
}
