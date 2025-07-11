package com.oronaminc.join.room.service;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oronaminc.join.document.domain.Document;
import com.oronaminc.join.document.service.DocumentService;
import com.oronaminc.join.emoji.service.EmojiService;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.question.service.QuestionService;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.domain.RoomStatus;
import com.oronaminc.join.room.dto.CreateRoomRequest;
import com.oronaminc.join.room.dto.CreateRoomResponse;
import com.oronaminc.join.room.dto.JoinRoomRequest;
import com.oronaminc.join.room.dto.JoinRoomResponse;
import com.oronaminc.join.room.dto.RoomDetailResponse;
import com.oronaminc.join.room.dto.RoomUpdateInfoResponse;
import com.oronaminc.join.room.dto.RoomUpdateRequest;
import com.oronaminc.join.room.dto.RoomUpdateStatusRequest;
import com.oronaminc.join.room.util.CodeGenerator;
import com.oronaminc.join.room.util.RoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final ParticipantService participantService;
    private final DocumentService documentService;
    private final QuestionService questionService;
    private final EmojiService emojiService;

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

    public RoomDetailResponse getRoomDetail(Long memberId, Long roomId) {
        participantService.validateParticipant(memberId, roomId);

        Room room = this.getRoomById(roomId);

        Participant presenter = participantService.getPresenter(roomId);
        List<Participant> team = participantService.getTeam(roomId);
        Document document = documentService.getDocumentByRoomId(roomId);

        return RoomMapper.toRoomDetailResponse(room, presenter, team, document, memberId);
    }

    public void updateRoom(Long memberId, Long roomId, RoomUpdateRequest updateRoomRequest) {
        participantService.validatePresenter(roomId, memberId);
        Room room = this.getRoomById(roomId);

        if (room.getRoomStatus().equals(RoomStatus.STARTED)) {
            throw new ErrorException(BAD_REQUEST_ROOM_STARTED);
        }

        room.update(updateRoomRequest);
        participantService.updateTeam(room, updateRoomRequest.teamEmail());
    }

    public void deleteRoom(Long memberId, Long roomId) {
        participantService.validatePresenter(roomId, memberId);
        Room room = this.getRoomById(roomId);

        if (room.getRoomStatus().equals(RoomStatus.STARTED)) {
            throw new ErrorException(BAD_REQUEST_ROOM_STARTED);
        }

        participantService.deleteParticipantByRoomId(roomId);
        questionService.deleteByRoomId(roomId);
        emojiService.deleteByRoomEmoji(roomId);
        documentService.deleteByRoomId(roomId);
        roomRepository.deleteById(roomId);
    }

    public void updateRoomStatus(Long memberId, Long roomId, RoomUpdateStatusRequest roomUpdateStatusRequest) {
        participantService.validatePresenter(roomId, memberId);
        Room room = this.getRoomById(roomId);

        RoomStatus updateStatus = roomUpdateStatusRequest.roomStatus();
        List<RoomStatus> canUpdateStatus = List.of(RoomStatus.STARTED, RoomStatus.ENDED);
        if (!canUpdateStatus.contains(roomUpdateStatusRequest.roomStatus())) {
            throw new ErrorException(BAD_REQUEST_UPDATE_STATUS);
        }
        room.updateStatus(updateStatus);
    }

    public RoomUpdateInfoResponse getRoomUpdateInfo(Long memberId, Long roomId) {
        participantService.validatePresenter(roomId, memberId);
        Room room = this.getRoomById(roomId);
        List<Participant> team = participantService.getTeam(roomId);
        return RoomMapper.toRoomUpdateInfoResponse(room, team);
    }

    private Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ErrorException(NOT_FOUND_ROOM));
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
