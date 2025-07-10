package com.oronaminc.join.global.exception;

import static com.oronaminc.join.global.exception.ErrorStatus.INTERNAL_SERVER_ERROR;
import static com.oronaminc.join.global.exception.ErrorStatus.NOT_FOUND;
import static com.oronaminc.join.global.exception.ErrorStatus.UNAUTHORIZED;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 상황에 따라 추가
    NOT_FOUND_MEMBER("AUTH-001", "존재하지 않는 회원입니다.", NOT_FOUND),
    UNAUTHORIZED_MEMBER("AUTH-002", "인증되지 않은 회원입니다.", UNAUTHORIZED),

    NOT_FOUND_ROOM("ROOM-001", "존재하지 않는 발표방입니다.", NOT_FOUND),

    NOT_FOUND_PARTICIPANT("PARTICIPANT-001", "발표방에 존재하지 않는 회원입니다.", NOT_FOUND),
    UNAUTHORIZED_TEAM_GUEST("PARTICIPANT-002", "게스트는 팀이 될 수 없습니다.", UNAUTHORIZED),

    FILE_UPLOAD_FAILED("FILE-001", "파일 업로드에 실패하였습니다.", INTERNAL_SERVER_ERROR),
    NOT_FOUND_FILE("FILE-002", "존재하지 않는 파일입니다.", NOT_FOUND),

    UNAUTHORIZED_ROLE_ANSWER("ANSWER-001", "팀원 또는 발표자만 댓글을 작성할 수 있습니다.", UNAUTHORIZED);

    private final String code;
    private final String message;
    private final ErrorStatus errorStatus;
}
