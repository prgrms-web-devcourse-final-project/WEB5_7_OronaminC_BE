package com.oronaminc.join.room.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.oronaminc.join.room.service.RoomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

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
}
