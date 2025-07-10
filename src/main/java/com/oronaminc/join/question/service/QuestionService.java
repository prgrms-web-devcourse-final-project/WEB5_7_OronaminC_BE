package com.oronaminc.join.question.service;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;

import com.oronaminc.join.answer.service.AnswerService;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.question.dao.QuestionRepository;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.dto.QuestionCreateRequest;
import com.oronaminc.join.question.mapper.QuestionMapper;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final ParticipantRepository participantRepository;
    private final AnswerService answerService;

    public Question create(Long roomId, Long memberId, QuestionCreateRequest requestDto) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_MEMBER));

        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_ROOM));

        if (!participantRepository.existsByRoomIdAndMemberId(room.getId(), member.getId())) {
            throw new ErrorException(NOT_FOUND_PARTICIPANT);
        }

        Question question = QuestionMapper.toQuestion(room, member, requestDto);

        questionRepository.save(question);

        return question;
    }

    public void deleteByRoomId(Long roomId) {
        List<Question> questions = questionRepository.findByRoomId(roomId);
        answerService.deleteByQuestionList(questions);
        questionRepository.deleteByRoomId(roomId);
    }
}
