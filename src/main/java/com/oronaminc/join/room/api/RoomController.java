package com.oronaminc.join.room.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.oronaminc.join.room.dto.ReportResponse;
import com.oronaminc.join.room.dto.RoomDetailResponse;
import com.oronaminc.join.room.dto.RoomUpdateInfoResponse;
import com.oronaminc.join.room.dto.RoomUpdateRequest;
import com.oronaminc.join.room.dto.RoomUpdateStatusRequest;
import com.oronaminc.join.room.service.RoomService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "발표방")
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

    @Operation(
            summary = "비밀코드로 방 입장",
            description = "비밀코드를 통해 해당 발표방에 참가자로 등록합니다. 시작 전 상태이면 참가할 수 없습니다.",
            security = @SecurityRequirement(name = "sessionAuth")
    )
    @GetMapping("/code")
    @ResponseStatus(HttpStatus.OK)
    public JoinRoomResponse joinRoom(
            @RequestBody JoinRoomRequest joinRoomRequest,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        return roomService.joinRoom(memberDetails.getId(), joinRoomRequest);
    }

    @Operation(
            summary = "발표방 상세 조회",
            description = "발표방을 상세 조회합니다. 비밀코드로 방 입장을 통해 참가자로 등록되었거나 팀 혹은 생성자가 아니면 예외가 발생합니다. roomStatus에는 BEFORE_START, STARTED, ENDED가 있습니다.",
            security = @SecurityRequirement(name = "sessionAuth")
    )
    @GetMapping("/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    public RoomDetailResponse getRoomDetail(@PathVariable Long roomId, @AuthenticationPrincipal MemberDetails memberDetails) {
        return roomService.getRoomDetail(memberDetails.getId(), roomId);
    }

    @Operation(
            summary = "발표방 수정",
            description = "발표방을 수정합니다. 발표방 생성자만 가능합니다. 기존 값을 유지하고 싶으면 발표방 수정용 조회를 통해 가져온 값을 그대로 입력해주세요.",
            security = @SecurityRequirement(name = "sessionAuth")
    )
    @PatchMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRoom(
            @RequestBody @Valid RoomUpdateRequest roomUpdateRequest,
            @PathVariable Long roomId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        roomService.updateRoom(memberDetails.getId(), roomId, roomUpdateRequest);
    }

    @Operation(
            summary = "발표방 삭제",
            description = "발표방을 삭제합니다. 발표방 생성자만 가능합니다.",
            security = @SecurityRequirement(name = "sessionAuth")
    )
    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable Long roomId, @AuthenticationPrincipal MemberDetails memberDetails) {
        roomService.deleteRoom(memberDetails.getId(), roomId);
    }

    @Operation(
            summary = "발표방 상태변경",
            description = "발표방의 상태를 변경합니다.",
            security = @SecurityRequirement(name = "sessionAuth")
    )
    @PatchMapping("/{roomId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRoomStatus(
            @PathVariable Long roomId,
            @RequestBody RoomUpdateStatusRequest roomUpdateStatusRequest,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        roomService.updateRoomStatus(memberDetails.getId(), roomId, roomUpdateStatusRequest);
    }

    @Operation(
            summary = "발표방 수정용 조회",
            description = "발표방 수정용 조회입니다. 발표방 수정에서 이 값을 그대로 보내주시면 수정되지 않습니다.",
            security = @SecurityRequirement(name = "sessionAuth")
    )
    @GetMapping("/{roomId}/update")
    @ResponseStatus(HttpStatus.OK)
    public RoomUpdateInfoResponse getUpdateInfo(
            @PathVariable Long roomId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        return roomService.getRoomUpdateInfo(memberDetails.getId(), roomId);
    }

    @Operation(
            summary = "리포트 조회",
            description = "리포트에 필요한 데이터 조회입니다. 생성자만 조회 가능합니다.",
            security = @SecurityRequirement(name = "sessionAuth")
    )
    @GetMapping("/{roomId}/report")
    @ResponseStatus(HttpStatus.OK)
    public ReportResponse getReport(
            @PathVariable Long roomId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        return roomService.getRoomReport(roomId, memberDetails.getId());
    }
}
