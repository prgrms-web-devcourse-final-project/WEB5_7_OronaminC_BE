package com.oronaminc.join.global.exception;

import static com.oronaminc.join.global.exception.ErrorStatus.BAD_REQUEST;
import static com.oronaminc.join.global.exception.ErrorStatus.FORBIDDEN;
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

    NOT_FOUND_ROOM_QUESTION("QUESTION-001", "질문을 해당 방에서 찾을 수 없습니다.", NOT_FOUND),
    NOT_FOUND_QUESTION("QUESTION-002", "질문을 찾을 수 없습니다.", NOT_FOUND),
    UNAUTHORIZED_EDIT_QUESTION("QUESTION-003", "작성자만 질문을 수정할 수 있습니다.", UNAUTHORIZED),
    UNAUTHORIZED_DELETE_QUESTION("QUESTION-003", "작성자 및 관리자만 질문을 삭제할 수 있습니다.", UNAUTHORIZED),

    UNAUTHORIZED_ROLE_ANSWER("ANSWER-001", "팀원 또는 발표자만 댓글을 작성할 수 있습니다.", UNAUTHORIZED),
    NOT_FOUND_EXIST_ANSWER("ANSWER-002", "해당 질문에 대한 답변이 존재하지 않습니다.", NOT_FOUND),
    NOT_FOUND_ANSWER("ANSWER-003", "답변이 존재하지 않습니다.", NOT_FOUND),
    BADREQUEST_DUPLICATION_ANSWER("ANSWER-004", "이미 답변한 질문입니다.", BAD_REQUEST),


    ACCESS_DENIED_SESSION("SESSION-1201", "접근 권한이 없습니다.", FORBIDDEN),
    NOT_FOUND_SESSION("SESSION-1202", "세션이 유효하지 않습니다.", UNAUTHORIZED),
    EXPIRED_SESSION("SESSION-1203", "세션이 만료되었습니다.", UNAUTHORIZED),

    SOCKET_ERROR("SOCKET-3000", "웹소켓 연결 중 서버 오류가 발생했습니다.", INTERNAL_SERVER_ERROR),
    SOCKET_RUNTIME_ERROR("SOCKET-2000", "처리되지 않은 오류가 발생했습니다", INTERNAL_SERVER_ERROR),
    SOCKET_VALIDATION_ERROR("SOCKET-1001", "입력값이 유효하지 않습니다.", BAD_REQUEST);

    private final String code;
    private final String message;
    private final ErrorStatus errorStatus;
}
