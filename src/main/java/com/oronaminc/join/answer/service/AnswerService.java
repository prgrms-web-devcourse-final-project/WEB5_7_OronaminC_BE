package com.oronaminc.join.answer.service;

import static com.oronaminc.join.global.exception.ErrorCode.BADREQUEST_DUPLICATION_ANSWER;
import static com.oronaminc.join.global.exception.ErrorCode.NOT_FOUND_PARTICIPANT;

import com.oronaminc.join.answer.dto.AnswerGetResponse;
import com.oronaminc.join.answer.mapper.AnswerMapper;
import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.emoji.repository.EmojiRepository;
import java.util.List;

import org.springframework.stereotype.Service;
import com.oronaminc.join.answer.dao.AnswerRepository;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerCreateRequest;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.question.dao.QuestionRepository;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final QuestionRepository questionRepository;
    private final ParticipantRepository participantRepository;
    private final EmojiRepository emojiRepository;

    @Transactional
    public Answer create(Long roomId, Long memberId, Long questionId ,AnswerCreateRequest requestDto ){

        Member member = getMember(memberId);

        Room room = getRoom(roomId);

        Question question = getRoomQuestion(questionId, roomId);

        if (!participantRepository.existsByRoomIdAndMemberId(room.getId(), member.getId())) {
            throw new ErrorException(NOT_FOUND_PARTICIPANT);
        }

        if(answerRepository.existsByQuestionIdAndMemberId(question.getId(), member.getId())){
            throw new ErrorException(BADREQUEST_DUPLICATION_ANSWER);
        }

        Answer answer = AnswerMapper.toEntity(question, member, requestDto);

        answerRepository.save(answer);

        return answer;
    }

    @Transactional
    public AnswerGetResponse getAnswer(Long roomId, Long questionId, Long memberId) {
        getMember(memberId);
        getRoom(roomId);
        getRoomQuestion(questionId, roomId);
        Answer answer = getExistAnswer(questionId);

        int emojiCount = emojiRepository.countByTargetIdAndTargetType(answer.getId(), TargetType.ANSWER);
        boolean isEmojied = emojiRepository.findByMemberIdAndTargetIdAndTargetType(memberId, answer.getId(), TargetType.ANSWER).isPresent();

        return AnswerMapper.toAnswerGetResponse(answer, emojiCount, isEmojied);
    }

    public void deleteByQuestionList(List<Question> questions) {
        answerRepository.deleteByQuestionIn(questions);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_MEMBER));
    }

    private Room getRoom(Long roomId) {
        return roomRepository.findById(roomId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_ROOM));
    }

    private Question getRoomQuestion(Long questionId, Long roomId) {
        getQuestion(questionId);
        return questionRepository.findByIdAndRoomId(questionId, roomId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_ROOM_QUESTION));
    }

    private Question getQuestion(Long questionId) {
        return questionRepository.findById(questionId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_QUESTION));
    }

    private Answer getExistAnswer(Long questionId) {
        return answerRepository.findByQuestionId(questionId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_EXIST_ANSWER));
    }

}
