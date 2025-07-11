package com.oronaminc.join.answer.service;

import static com.oronaminc.join.global.exception.ErrorCode.BADREQUEST_DUPLICATION_ANSWER;
import static com.oronaminc.join.global.exception.ErrorCode.NOT_FOUND_PARTICIPANT;

import com.oronaminc.join.answer.dao.AnswerRepository;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerCreateRequest;
import com.oronaminc.join.answer.mapper.AnswerMapper;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.question.dao.QuestionRepository;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.service.QuestionService;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final QuestionRepository questionRepository;
    private final ParticipantRepository participantRepository;

    public Answer create(Long roomId, Long memberId, Long questionId ,AnswerCreateRequest requestDto ){

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_MEMBER));

        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_ROOM));

        Question question = questionRepository.findByIdAndRoomId(questionId, roomId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_QUESTION));

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

}
