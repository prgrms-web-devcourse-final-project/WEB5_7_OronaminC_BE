package com.oronaminc.join.room.service;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import java.util.List;

import com.oronaminc.join.answer.service.AnswerReader;
import com.oronaminc.join.infra.service.S3Service;
import com.oronaminc.join.participant.service.ParticipantReader;
import com.oronaminc.join.question.service.QuestionReader;
import com.oronaminc.join.room.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oronaminc.join.document.domain.Document;
import com.oronaminc.join.document.service.DocumentReader;
import com.oronaminc.join.document.service.DocumentService;
import com.oronaminc.join.emoji.service.EmojiService;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.question.service.QuestionService;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.domain.RoomStatus;
import com.oronaminc.join.room.util.CodeGenerator;
import com.oronaminc.join.room.util.RoomMapper;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final ParticipantService participantService;
    private final DocumentService documentService;
    private final QuestionService questionService;
    private final DocumentReader documentReader;
    private final EmojiService emojiService;
    private final S3Service s3Service;
    private final RoomReader roomReader;
    private final AnswerReader answerReader;
    private final ParticipantReader participantReader;
    private final QuestionReader questionReader;


    private static final int CODE_LENGTH = 6;

    public CreateRoomResponse createRoom(CreateRoomRequest createRoomRequest,
        String presenterEmail) {
        String code = this.generateCode();
        Room room = RoomMapper.toRoom(createRoomRequest, code);
        roomRepository.save(room);

        documentService.saveDocument(createRoomRequest.documentUrl(), room);
        participantService.savePresenterAndTeam(presenterEmail, createRoomRequest.teamEmail(), room);

        return RoomMapper.toCreateRoomResponse(room);
    }

    public JoinRoomResponse joinRoom(Long memberId, JoinRoomRequest joinRoomRequest) {
        Room room = roomReader.getBySecretCode(joinRoomRequest.secretCode());

        participantService.saveParticipantById(memberId, room, ParticipantType.GUEST);
        return new JoinRoomResponse(room.getId());
    }

    public RoomDetailResponse getRoomDetail(Long memberId, Long roomId) {
        participantService.validateParticipant(memberId, roomId);

        Room room = roomReader.getById(roomId);

        Participant presenter = participantService.getPresenter(roomId);
        List<Participant> team = participantService.getTeam(roomId);
        Document document = documentReader.getByRoomId(roomId);

        String presignedUrl = s3Service.generatePresignedUrl(document.getFileUrl());

        return RoomMapper.toRoomDetailResponse(room, presenter, team, presignedUrl, memberId);
    }

    public void updateRoom(Long memberId, Long roomId, RoomUpdateRequest updateRoomRequest) {
        participantService.validatePresenter(roomId, memberId);

        Room room = roomReader.getById(roomId);
        Document document = documentReader.getByRoomId(roomId);

        if (room.getRoomStatus().equals(RoomStatus.STARTED)) {
            throw new ErrorException(BAD_REQUEST_ROOM_STARTED);
        }

        document.update(updateRoomRequest.documentUrl());
        room.update(updateRoomRequest);
        participantService.updateTeam(room, updateRoomRequest.teamEmail());
    }

    public void deleteRoom(Long memberId, Long roomId) {
        participantService.validatePresenter(roomId, memberId);

        Room room = roomReader.getById(roomId);
        Document document = documentReader.getByRoomId(roomId);

        if (room.getRoomStatus().equals(RoomStatus.STARTED)) {
            throw new ErrorException(BAD_REQUEST_ROOM_STARTED);
        }

        participantService.deleteParticipantByRoomId(roomId);
        questionService.deleteByRoomId(roomId);
        emojiService.deleteByRoomEmoji(roomId);
        // S3 버킷 내 파일 삭제
        s3Service.deleteFile(document.getFileUrl());
        documentService.deleteByRoomId(roomId);
        roomRepository.deleteById(roomId);
    }

    public void updateRoomStatus(Long memberId, Long roomId,
        RoomUpdateStatusRequest roomUpdateStatusRequest) {
        participantService.validatePresenter(roomId, memberId);
        Room room = roomReader.getById(roomId);

        RoomStatus updateStatus = roomUpdateStatusRequest.roomStatus();
        List<RoomStatus> canUpdateStatus = List.of(RoomStatus.STARTED, RoomStatus.ENDED);
        if (!canUpdateStatus.contains(roomUpdateStatusRequest.roomStatus())) {
            throw new ErrorException(BAD_REQUEST_UPDATE_STATUS);
        }
        room.updateStatus(updateStatus);
    }

    public RoomUpdateInfoResponse getRoomUpdateInfo(Long memberId, Long roomId) {
        participantService.validatePresenter(roomId, memberId);
        Room room = roomReader.getById(roomId);
        List<Participant> team = participantService.getTeam(roomId);
        return RoomMapper.toRoomUpdateInfoResponse(room, team);
    }

    public Integer getRoomParticipantLimit(Long roomId) {
        return roomReader.getById(roomId).getParticipantLimit();
    }

    private String generateCode() {
        while (true) {
            String code = CodeGenerator.generateCode(CODE_LENGTH);
            if (!roomReader.existsBySecretCode(code)) {
                return code;
            }
        }
    }

    public ReportResponse getRoomReport(Long roomId, Long memberId) {

        Participant participant = participantService.getPresenter(roomId);

        if (!participant.getMember().getId().equals(memberId)) {
            throw new ErrorException(UNAUTHORIZED_REPORT_READ);
        }

        Room room = roomReader.getById(roomId);
        Long totalView = participantReader.countTotalView(roomId);
        Long totalQuestions = questionReader.countByRoomId(roomId);
        Long totalAnswerByQuestion = answerReader.countAnsweredQuestionsByRoomId(roomId);
        Double answerRate = calculateAnswerRate(totalQuestions, totalAnswerByQuestion);
        List<TopQnADto> top3QnA = questionReader.findTop3QnA(roomId);

        return RoomMapper.toReportResponse(room, totalView,totalQuestions, answerRate, top3QnA);
    }

    private Double calculateAnswerRate(Long totalQuestions, Long totalAnswerByQuestion) {
        return (totalQuestions == 0)
                ? 0.0
                : ((double) totalAnswerByQuestion / totalQuestions) * 100;

    }
}
