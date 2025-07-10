package com.oronaminc.join.room.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.oronaminc.join.member.security.MemberDetails;
import com.oronaminc.join.room.dto.CreateRoomRequest;
import com.oronaminc.join.room.dto.CreateRoomResponse;
import com.oronaminc.join.room.dto.JoinRoomRequest;
import com.oronaminc.join.room.dto.JoinRoomResponse;
import com.oronaminc.join.room.dto.RoomDetailResponse;
import com.oronaminc.join.room.dto.RoomUpdateRequest;
import com.oronaminc.join.room.service.RoomService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @Operation(
        summary = "발표방 생성",
        description = "발표 제목, 소개, 팀원 목록 등을 포함해 새로운 발표방을 생성합니다. 로그인한 발표자만 생성할 수 있으며, 응답으로 방 ID와 비밀코드를 반환합니다.",
        security = @SecurityRequirement(name = "sessionAuth"),
        responses = {
            @ApiResponse(responseCode = "201", description = "발표방 생성 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 형식 오류"),
            @ApiResponse(responseCode = "401", description = "로그인되지 않은 사용자")
        }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateRoomResponse createRoom(
            @RequestBody @Valid CreateRoomRequest createRoomRequest,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        String presenterEmail = memberDetails.getName();
        return roomService.createRoom(createRoomRequest, presenterEmail);
    }

    @GetMapping("/code")
    @ResponseStatus(HttpStatus.OK)
    public JoinRoomResponse joinRoom(
            @RequestBody JoinRoomRequest joinRoomRequest,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        return roomService.joinRoom(memberDetails.getId(), joinRoomRequest);
    }

    @GetMapping("/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    public RoomDetailResponse getRoomDetail(@PathVariable Long roomId, @AuthenticationPrincipal MemberDetails memberDetails) {
        return roomService.getRoomDetail(memberDetails.getId(), roomId);
    }

    @PatchMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRoom(
            @RequestBody @Valid RoomUpdateRequest roomUpdateRequest,
            @PathVariable Long roomId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        roomService.updateRoom(memberDetails.getId(), roomId, roomUpdateRequest);
    }
}
