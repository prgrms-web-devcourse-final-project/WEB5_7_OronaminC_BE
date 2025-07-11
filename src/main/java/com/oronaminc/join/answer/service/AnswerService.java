package com.oronaminc.join.answer.service;

import static com.oronaminc.join.global.exception.ErrorCode.BADREQUEST_DUPLICATION_ANSWER;

import com.oronaminc.join.answer.dao.AnswerRepository;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerCreateRequest;
import com.oronaminc.join.answer.dto.AnswerGetResponse;
import com.oronaminc.join.answer.mapper.AnswerMapper;
import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.emoji.service.EmojiReader;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.service.MemberReader;
import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.service.QuestionReader;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.service.RoomReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final ParticipantService participantService;
    private final QuestionReader questionReader;
    private final MemberReader memberReader;
    private final AnswerReader answerReader;
    private final RoomReader roomReader;
    private final EmojiReader emojiReader;

    @Transactional
    public Answer create(Long roomId, Long memberId, Long questionId,
        AnswerCreateRequest requestDto) {

        Member member = memberReader.getById(memberId);
        Room room = roomReader.getById(roomId);
        Question question = questionReader.getByIdAndRoomId(questionId, roomId);

        participantService.validateParticipant(member.getId(), room.getId());

        if (answerReader.existsByQuestionIdAndMemberId(question.getId(), member.getId())) {
            throw new ErrorException(BADREQUEST_DUPLICATION_ANSWER);
        }

        Answer answer = AnswerMapper.toEntity(question, member, requestDto);

        answerRepository.save(answer);

        return answer;
    }

    @Transactional
    public AnswerGetResponse getAnswer(Long roomId, Long questionId, Long memberId) {
        Member member = memberReader.getById(memberId);
        roomReader.getById(roomId);
        questionReader.getByIdAndRoomId(questionId, roomId);
        Answer answer = answerReader.getExistById(questionId);

        int emojiCount = emojiReader.countByTargetIdAndTargetType(answer.getId(),
            TargetType.ANSWER);
        boolean isEmojied = emojiReader.findByMemberIdAndTargetIdAndTargetType(member.getId(),
            answer.getId(), TargetType.ANSWER).isPresent();

        return AnswerMapper.toAnswerGetResponse(answer, emojiCount, isEmojied);
    }

    public void deleteByQuestionList(List<Question> questions) {
        answerRepository.deleteByQuestionIn(questions);
    }

}
