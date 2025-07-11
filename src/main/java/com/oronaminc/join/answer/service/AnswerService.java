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
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.service.MemberReader;
import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.service.QuestionReader;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionReader questionReader;
    private final MemberReader memberReader;
    private final ParticipantService participantService;

    @Transactional
    public Answer create(Long roomId, Long memberId, Long questionId ,AnswerCreateRequest requestDto ){

        Member member = memberReader.getById(memberId);

        Question question = questionReader.getByIdAndRoomId(questionId, roomId);

        participantService.validateParticipant(memberId, roomId);

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
