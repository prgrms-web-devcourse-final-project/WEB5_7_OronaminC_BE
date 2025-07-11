package com.oronaminc.join.answer.service;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oronaminc.join.answer.dao.AnswerRepository;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerCreateRequest;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.service.QuestionReader;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.service.RoomReader;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final MemberRepository memberRepository;
    private final ParticipantRepository participantRepository;
    private final RoomReader roomReader;
    private final QuestionReader questionReader;

    @Transactional
    public Answer create(Long roomId, Long memberId, Long questionId ,AnswerCreateRequest requestDto ){

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_MEMBER));

        Room room = roomReader.getById(roomId);

        Question question = questionReader.getByIdAndRoomId(questionId, roomId);

        if (!participantRepository.existsByRoomIdAndMemberId(room.getId(), member.getId())) {
            throw new ErrorException(NOT_FOUND_PARTICIPANT);
        }

        if(answerRepository.existsByQuestionIdAndMemberId(question.getId(), member.getId())){
            throw new ErrorException(BADREQUEST_DUPLICATION_ANSWER);
        }

        Answer answer = Answer.create(question, member, requestDto);

        answerRepository.save(answer);

        return answer;
    }

    public void deleteByQuestionList(List<Question> questions) {
        answerRepository.deleteByQuestionIn(questions);
    }

    public Answer findById(Long id) {
        return answerRepository.findById(id)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_ROOM));
    }
}
