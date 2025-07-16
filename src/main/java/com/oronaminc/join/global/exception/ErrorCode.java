package com.oronaminc.join.global.exception;

import static com.oronaminc.join.global.exception.ErrorStatus.BAD_REQUEST;
import static com.oronaminc.join.global.exception.ErrorStatus.CONFLICT;
import static com.oronaminc.join.global.exception.ErrorStatus.FORBIDDEN;
import static com.oronaminc.join.global.exception.ErrorStatus.INTERNAL_SERVER_ERROR;
import static com.oronaminc.join.global.exception.ErrorStatus.NOT_FOUND;
import static com.oronaminc.join.global.exception.ErrorStatus.TOO_MANY_REQUESTS;
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
    UNAUTHORIZED_JOIN_ROOM("ROOM-004", "시작 전 방에 참가할 수 없습니다.", UNAUTHORIZED),
    UNAUTHORIZED_SUBSCRIBE_ROOM("ROOM-005", "시작 전 혹은 종료된 방에 참가할 수 없습니다.", UNAUTHORIZED),

    NOT_FOUND_PARTICIPANT("PARTICIPANT-001", "발표방에 존재하지 않는 회원입니다.", NOT_FOUND),
    UNAUTHORIZED_TEAM_GUEST("PARTICIPANT-002", "게스트는 팀이 될 수 없습니다.", UNAUTHORIZED),
    UNAUTHORIZED_UPDATE_AND_DELETE("PARTICIPANT-003", "발표방 수정 및 삭제 권한이 없습니다.", UNAUTHORIZED),
    UNAUTHORIZED_REPORT_READ("PARTICIPANT-004", "결과 리포트 조회 권한이 없습니다.", UNAUTHORIZED),
    UNAUTHORIZED_LIMIT_PARTICIPANT("PARTICIPANT-005", "인원이 가득 차 참가할 수 없습니다.", UNAUTHORIZED),
    UNAUTHORIZED_NOT_JOIN_ROOM("PARTICIPANT-005", "발표방에 참여하지 않았습니다. 먼저 참여해주세요.", UNAUTHORIZED),


    FILE_UPLOAD_FAILED("FILE-001", "파일 업로드에 실패하였습니다.", INTERNAL_SERVER_ERROR),
    NOT_FOUND_FILE("FILE-002", "존재하지 않는 파일입니다.", NOT_FOUND),

    NOT_FOUND_ROOM_QUESTION("QUESTION-001", "질문을 해당 방에서 찾을 수 없습니다.", NOT_FOUND),
    NOT_FOUND_QUESTION("QUESTION-002", "질문을 찾을 수 없습니다.", NOT_FOUND),
    UNAUTHORIZED_EDIT_QUESTION("QUESTION-003", "작성자만 질문을 수정할 수 있습니다.", UNAUTHORIZED),
    UNAUTHORIZED_DELETE_QUESTION("QUESTION-004", "작성자 및 관리자만 질문을 삭제할 수 있습니다.", UNAUTHORIZED),
    TOO_MANY_REQUESTS_QUESTION("QUESTION-005", "잠시 후 다시 시도해주세요.", TOO_MANY_REQUESTS),

    UNAUTHORIZED_ROLE_ANSWER("ANSWER-001", "팀원 또는 발표자만 댓글을 작성할 수 있습니다.", UNAUTHORIZED),
    NOT_FOUND_EXIST_ANSWER("ANSWER-002", "해당 질문에 대한 답변이 존재하지 않습니다.", NOT_FOUND),
    NOT_FOUND_ANSWER("ANSWER-003", "답변이 존재하지 않습니다.", NOT_FOUND),
    BADREQUEST_DUPLICATION_ANSWER("ANSWER-004", "이미 답변한 질문입니다.", BAD_REQUEST),


    ACCESS_DENIED_SESSION("SESSION-1201", "접근 권한이 없습니다.", FORBIDDEN),
    NOT_FOUND_SESSION("SESSION-1202", "세션이 유효하지 않습니다.", UNAUTHORIZED),
    EXPIRED_SESSION("SESSION-1203", "세션이 만료되었습니다.", UNAUTHORIZED),

    SOCKET_ERROR("SOCKET-3000", "웹소켓 연결 중 서버 오류가 발생했습니다.", INTERNAL_SERVER_ERROR),
    SOCKET_RUNTIME_ERROR("SOCKET-2000", "처리되지 않은 오류가 발생했습니다", INTERNAL_SERVER_ERROR),
    SOCKET_VALIDATION_ERROR("SOCKET-1001", "입력값이 유효하지 않습니다.", BAD_REQUEST),
    SOCKET_BAD_REQUEST_PATH("SOCKET-1002", "경로가 유효하지 않습니다.", BAD_REQUEST),
    SOCKET_BAD_REQUEST_MEMBER("SOCKET-1003", "회원이 유효하지 않습니다.", BAD_REQUEST),


    CONFLICT_EMOJI("EMOJI-001", "공감 처리 중 충돌이 발생했습니다.", CONFLICT),
    NOT_FOUND_EMOJI("EMOJI-002", "해당 이모지가 존재하지 않습니다.", NOT_FOUND),
    TOO_MANY_REQUESTS_EMOJI("EMOJI-003", "잠시 후 다시 시도해주세요.", TOO_MANY_REQUESTS),
    ALREADY_EXISTS_EMOJI("EMOJI-004", "이미 해당 이모지가 존재합니다.", CONFLICT)
    ;

    private final String code;
    private final String message;
    private final ErrorStatus errorStatus;
}
