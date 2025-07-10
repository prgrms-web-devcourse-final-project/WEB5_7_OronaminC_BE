package com.oronaminc.join.answer.dto;

public record AnswerCreateRequest(
    //TODO: 빈값 or " " (space) 처리
    String content
) {

}
