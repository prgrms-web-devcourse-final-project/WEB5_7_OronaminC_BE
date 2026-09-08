package com.oronaminc.join.answer.util;

import com.oronaminc.join.global.exception.ErrorCode;
import lombok.Getter;


@Getter
public enum PermissionType {
    CREATE, DELETE;

    public ErrorCode toErrorCode() {
        return switch (this) {
            case CREATE -> ErrorCode.UNAUTHORIZED_ROLE_ANSWER;
            case DELETE -> ErrorCode.UNAUTHORIZED_DELETE_ANSWER;
        };
    }

}
