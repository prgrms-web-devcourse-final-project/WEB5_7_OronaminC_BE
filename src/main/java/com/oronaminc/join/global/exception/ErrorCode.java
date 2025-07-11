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
    BAD_REQUEST_ROOM_STARTED("ROOM-002", "시작 상태의 발표방은 수정 및 삭제할 수 없습니다.", BAD_REQUEST),
    BAD_REQUEST_UPDATE_STATUS("ROOM-003", "변경할 수 없는 상태입니다.", BAD_REQUEST),

    NOT_FOUND_PARTICIPANT("PARTICIPANT-001", "발표방에 존재하지 않는 회원입니다.", NOT_FOUND),
    UNAUTHORIZED_TEAM_GUEST("PARTICIPANT-002", "게스트는 팀이 될 수 없습니다.", UNAUTHORIZED),
    UNAUTHORIZED_UPDATE_AND_DELETE("PARTICIPANT-003", "발표방 수정 및 삭제 권한이 없습니다.", UNAUTHORIZED),

    FILE_UPLOAD_FAILED("FILE-001", "파일 업로드에 실패하였습니다.", INTERNAL_SERVER_ERROR),
    NOT_FOUND_FILE("FILE-002", "존재하지 않는 파일입니다.", NOT_FOUND),

    NOT_FOUND_QUESTION("QUESTION-001", "존재하지 않는 질문입니다.", NOT_FOUND),

    NOT_FOUND_ANSWER("ANSWER-001", "존재하지 않는 답변입니다.", NOT_FOUND);

    private final String code;
    private final String message;
    private final ErrorStatus errorStatus;
}
