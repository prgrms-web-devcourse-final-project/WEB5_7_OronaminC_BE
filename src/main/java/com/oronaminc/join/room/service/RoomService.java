package com.oronaminc.join.room.service;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.dto.CreateRoomRequest;
import com.oronaminc.join.room.dto.CreateRoomResponse;
import com.oronaminc.join.room.dto.JoinRoomRequest;
import com.oronaminc.join.room.dto.JoinRoomResponse;
import com.oronaminc.join.room.util.CodeGenerator;
import com.oronaminc.join.room.util.RoomMapper;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final ParticipantService participantService;

    private static final int CODE_LENGTH = 6;

    public CreateRoomResponse createRoom(CreateRoomRequest createRoomRequest, String presenterEmail) {
        String code = this.generateCode();
        Room room = RoomMapper.toRoom(createRoomRequest, code);
        roomRepository.save(room);
        participantService.savePresenterAndTeam(presenterEmail, createRoomRequest.teamEmail(), room);
        return RoomMapper.toCreateRoomResponse(room);
    }

    public JoinRoomResponse joinRoom(Long memberId, JoinRoomRequest joinRoomRequest) {
        Room room = roomRepository.findBySecretCode(joinRoomRequest.secretCode())
                .orElseThrow(() -> new ErrorException(NOT_FOUND_ROOM));

        participantService.saveParticipantById(memberId, room, ParticipantType.GUEST);
        return new JoinRoomResponse(room.getId());
    }

    private String generateCode() {
        while(true){
            String code = CodeGenerator.generateCode(CODE_LENGTH);
            if (!roomRepository.existsBySecretCode(code)) {
                return code;
            }
        }
    }
}
