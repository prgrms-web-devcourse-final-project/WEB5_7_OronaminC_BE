package com.oronaminc.join.room.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.room.dao.RoomJpaRepository;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.dto.CreateRoomRequest;
import com.oronaminc.join.room.dto.CreateRoomResponse;
import com.oronaminc.join.room.util.CodeGenerator;
import com.oronaminc.join.room.util.RoomMapper;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomService {
    private final RoomJpaRepository roomJpaRepository;
    private final ParticipantService participantService;

    private static final int CODE_LENGTH = 6;

    public CreateRoomResponse createRoom(CreateRoomRequest createRoomRequest, String presenterEmail) {
        String code = CodeGenerator.generateCode(CODE_LENGTH);
        Room room = RoomMapper.toRoom(createRoomRequest, code);
        roomJpaRepository.save(room);
        participantService.savePresenterAndTeam(presenterEmail, createRoomRequest.teamEmail(), room);
        return RoomMapper.toCreateRoomResponse(room);
    }
}
