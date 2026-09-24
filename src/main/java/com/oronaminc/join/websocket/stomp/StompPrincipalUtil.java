package com.oronaminc.join.websocket.stomp;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import java.security.Principal;

import com.oronaminc.join.global.exception.ErrorException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StompPrincipalUtil {

    // CONNECT 시 StompAuthChannelInterceptor가 설정한 StompPrincipal(memberId)에서 회원 ID를 꺼냄
    public static Long getMemberId(Principal principal) {
        if (principal == null) {
            throw new ErrorException(UNAUTHORIZED_MEMBER);
        }
        return Long.valueOf(principal.getName());
    }
}
