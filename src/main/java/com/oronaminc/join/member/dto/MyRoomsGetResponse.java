package com.oronaminc.join.member.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record MyRoomsGetResponse(
    List<MyRoomsDto> content,
    int currentPage,
    int size,
    long totalElements,
    int totalPages
) {

}
