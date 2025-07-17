package com.oronaminc.join.room.service;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.service.AnswerReader;
import com.oronaminc.join.document.domain.Document;
import com.oronaminc.join.document.service.DocumentReader;
import com.oronaminc.join.document.service.DocumentService;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.infra.service.S3Service;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.participant.service.ParticipantReader;
import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.service.QuestionReader;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.domain.RoomStatus;
import com.oronaminc.join.room.dto.CreateRoomRequest;
import com.oronaminc.join.room.dto.CreateRoomResponse;
import com.oronaminc.join.room.dto.JoinRoomRequest;
import com.oronaminc.join.room.dto.JoinRoomResponse;
import com.oronaminc.join.room.dto.ReportResponse;
import com.oronaminc.join.room.event.RoomDeleteEvent;
import com.oronaminc.join.room.dto.RoomDetailResponse;
import com.oronaminc.join.room.dto.RoomJoinResponse;
import com.oronaminc.join.room.dto.RoomUpdateInfoResponse;
import com.oronaminc.join.room.dto.RoomUpdateRequest;
import com.oronaminc.join.room.dto.RoomUpdateStatusRequest;
import com.oronaminc.join.room.dto.TopQnAResponse;
import com.oronaminc.join.room.util.CodeGenerator;
import com.oronaminc.join.room.util.RoomMapper;
import com.oronaminc.join.websocket.session.CurrentParticipantManager;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final ParticipantService participantService;
    private final DocumentService documentService;
    private final S3Service s3Service;
    private final ParticipantReader participantReader;
    private final DocumentReader documentReader;
    private final QuestionReader questionReader;
    private final AnswerReader answerReader;
    private final RoomReader roomReader;
    private final CurrentParticipantManager currentParticipantManager;
    private final ApplicationEventPublisher publisher;

    private static final int CODE_LENGTH = 6;

    @Transactional
    public CreateRoomResponse createRoom(CreateRoomRequest createRoomRequest,
            String presenterEmail) {
        String code = this.generateCode();
        Room room = RoomMapper.toRoom(createRoomRequest, code);
        roomRepository.save(room);

        documentService.saveDocument(createRoomRequest.documentUrl(), room);
        participantService.savePresenterAndTeam(presenterEmail, createRoomRequest.teamEmail(), room);

        return RoomMapper.toCreateRoomResponse(room);
    }

    @Transactional
    public JoinRoomResponse joinRoom(Long memberId, JoinRoomRequest joinRoomRequest) {
        Room room = roomReader.getBySecretCode(joinRoomRequest.secretCode());
        if (room.getRoomStatus().equals(RoomStatus.BEFORE_START)) {
            throw new ErrorException(UNAUTHORIZED_JOIN_ROOM);
        }
        participantService.saveParticipantById(memberId, room, ParticipantType.GUEST);
        return new JoinRoomResponse(room.getId());
    }

    public RoomDetailResponse getRoomDetail(Long memberId, Long roomId) {
        participantService.validateParticipant(memberId, roomId);

        Room room = roomReader.getById(roomId);

        Participant presenter = participantService.getPresenter(roomId);
        List<Participant> team = participantService.getTeam(roomId);
        Document document = documentReader.getByRoomId(roomId);
        int participantCount = currentParticipantManager.getRoomParticipants(roomId).size();
        String presignedUrl = s3Service.generatePresignedUrl(document.getFileUrl());

        return RoomMapper.toRoomDetailResponse(room, presenter, team, presignedUrl, memberId, participantCount);
    }

    @Transactional
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

    @Transactional
    public void deleteRoom(Long memberId, Long roomId) {
        participantService.validatePresenter(roomId, memberId);

        Room room = roomReader.getById(roomId);
        Document document = documentReader.getByRoomId(roomId);

        if (room.getRoomStatus().equals(RoomStatus.STARTED)) {
            throw new ErrorException(BAD_REQUEST_ROOM_STARTED);
        }

        publisher.publishEvent(new RoomDeleteEvent(roomId));
        roomRepository.deleteById(roomId);
        s3Service.deleteFile(document.getFileUrl());
    }

    @Transactional
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

    @Transactional
    public RoomUpdateInfoResponse getRoomUpdateInfo(Long memberId, Long roomId) {
        participantService.validatePresenter(roomId, memberId);
        Room room = roomReader.getById(roomId);
        List<Participant> team = participantService.getTeam(roomId);
        return RoomMapper.toRoomUpdateInfoResponse(room, team);
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
        List<TopQnAResponse> topQnA = getTopQnA(roomId);

        return RoomMapper.toReportResponse(room, totalView, totalQuestions, answerRate, topQnA);
    }

    private List<TopQnAResponse> getTopQnA(Long roomId) {
        // top3 질문 리스트
        List<Question> top3Question = questionReader.findTop3Question(roomId);

        // top3 질문 id
        List<Long> questionIds = top3Question.stream()
                .map(Question::getId)
                .toList();

        // top3에 질문의 답변 조회
        List<Answer> answerByQuestionIds = answerReader.getAnswerByQuestionIds(questionIds);

        // 답변을 질문 ID 기준으로 그룹화
        Map<Long, List<String>> answersByQuestionId = answerByQuestionIds.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getQuestion().getId(),
                        Collectors.mapping(Answer::getContent, Collectors.toList())
                ));

        return top3Question.stream()
                .map(q -> new TopQnAResponse(
                        q.getContent(),
                        q.getEmojiCount(),
                        answersByQuestionId.getOrDefault(q.getId(), List.of())
                ))
                .toList();
    }

    private Double calculateAnswerRate(Long totalQuestions, Long totalAnswerByQuestion) {
        return (totalQuestions == 0)
                ? 0.0
                : ((double)totalAnswerByQuestion / totalQuestions) * 100;

    }

    public RoomJoinResponse subscribeRoom(Long roomId, Long memberId) {
        participantService.validateParticipant(memberId, roomId);
        Room room = roomReader.getById(roomId);
        if (!room.getRoomStatus().canSubscribeRoom) {
            throw new ErrorException(UNAUTHORIZED_SUBSCRIBE_ROOM);
        }
        Integer limit = room.getParticipantLimit();
        currentParticipantManager.addParticipant(roomId, memberId, limit);
        return new RoomJoinResponse(currentParticipantManager.getRoomParticipants(roomId).size());
    }
}
