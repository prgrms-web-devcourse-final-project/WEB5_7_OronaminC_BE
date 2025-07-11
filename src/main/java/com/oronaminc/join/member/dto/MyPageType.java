package com.oronaminc.join.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "발표방 조회 타입: ALL(모두), CREATED(생성자), JOINED(참여자)")
public enum MyPageType {
    ALL, CREATED, JOINED
}
