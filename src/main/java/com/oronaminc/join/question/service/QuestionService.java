package com.oronaminc.join.question.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oronaminc.join.answer.service.AnswerService;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.global.util.SliceUtil;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.service.MemberReader;
import com.oronaminc.join.participant.service.ParticipantReader;
import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.question.dao.QuestionRepository;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.domain.QuestionSort;
import com.oronaminc.join.question.dto.QuestionAssembleResponse;
import com.oronaminc.join.question.dto.QuestionRequest;
import com.oronaminc.join.question.dto.QuestionFlatResponse;
import com.oronaminc.join.question.util.QuestionMapper;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.service.RoomReader;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ParticipantService participantService;
    private final AnswerService answerService;
    private final QuestionReader questionReader;
    private final MemberReader memberReader;
    private final RoomReader roomReader;
    private final ParticipantReader participantReader;

    @Transactional
    public Question create(Long roomId, Long memberId, QuestionRequest requestDto) {

        Member member = memberReader.getById(memberId);
        Room room = roomReader.getById(roomId);
        participantService.validateParticipant(memberId, roomId);

        Question question = QuestionMapper.toQuestion(room, member, requestDto);

        questionRepository.save(question);

        return question;
    }

    public Slice<QuestionAssembleResponse> getQuestions(
        QuestionSort sort,
        Long lastId,
        Long lastEmojiCount,
        int size,
        Long memberId,
        Long roomId
    ) {
        memberReader.getById(memberId);
        roomReader.getById(roomId);

        Pageable pageable = PageRequest.of(0, size + 1);

        List<QuestionFlatResponse> questions = questionReader.findQuestionsOrderBy(lastId,
            lastEmojiCount, memberId, roomId, sort,
            pageable);

        List<QuestionAssembleResponse> assembledList = questions.stream()
            .map(QuestionMapper::toQuestionListResponse).toList();

        return SliceUtil.toSlice(assembledList, PageRequest.of(0, size));
    }

    @Transactional
    public Question update(Long memberId, Long roomId, Long questionId, QuestionRequest request) {
        Question question = questionReader.getByIdAndRoomId(questionId, roomId);

        // 참여자가 아님
        if (!participantReader.existsByRoomIdAndMemberId(roomId, memberId)) {
            throw ErrorException.of(ErrorCode.NOT_FOUND_PARTICIPANT,
                "{}번 발표방에는 {}번 회원이 잠가 중이지 않습니다.", roomId, memberId);
        }

        // 작성자가 아님
        if (!question.getMember().getId().equals(memberId)) {
            throw ErrorException.of(ErrorCode.UNAUTHORIZED_EDIT_QUESTION,
                "{}번 회원은 {}번 질문을 수정할 권한이 없습니다.", memberId, questionId);
        }

        question.updateContent(request.content());

        return question;
    }

    @Transactional
    public Long delete(Long memberId, Long roomId, Long questionId) {
        Question question = questionReader.getByIdAndRoomId(questionId, roomId);

        // 참여자가 아님
        if (!participantReader.existsByRoomIdAndMemberId(roomId, memberId)) {
            throw ErrorException.of(ErrorCode.NOT_FOUND_PARTICIPANT,
                "{}번 발표방에는 {}번 회원이 잠가 중이지 않습니다.", roomId, memberId);
        }

        // 관리자가 아님 && 작성자도 아님
        if (!participantReader.existsPresenterOrTeamByMemberId(roomId, memberId)
            && !question.getMember().getId().equals(memberId)) {
            throw ErrorException.of(ErrorCode.UNAUTHORIZED_DELETE_QUESTION,
                "{}번 회원은 {}번 질문을 삭제할 권한이 없습니다.", memberId, questionId);
        }

        answerService.deleteByQuestion(questionId);
        questionRepository.deleteById(questionId);

        return question.getId();
    }

    @Transactional
    public void deleteByRoomId(Long roomId) {
        List<Question> questions = questionReader.findByRoomId(roomId);
        answerService.deleteByQuestionList(questions);
        questionRepository.deleteByRoomId(roomId);
    }

}
