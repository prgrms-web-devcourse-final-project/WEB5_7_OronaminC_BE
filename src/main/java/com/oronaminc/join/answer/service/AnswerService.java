package com.oronaminc.join.answer.service;


import com.oronaminc.join.answer.dao.AnswerRepository;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerGetResponse;
import com.oronaminc.join.answer.dto.AnswerRequest;
import com.oronaminc.join.answer.mapper.AnswerMapper;
import com.oronaminc.join.answer.util.PermissionValidator;
import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.emoji.service.EmojiReader;
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
    private final QuestionReader questionReader;
    private final MemberReader memberReader;
    private final AnswerReader answerReader;
    private final RoomReader roomReader;
    private final EmojiReader emojiReader;
    private final PermissionValidator permissionValidator;

    @Transactional
    public Answer create(Long roomId, Long memberId, Long questionId,
        AnswerRequest request) {

        Member member = memberReader.getById(memberId);
        Room room = roomReader.getById(roomId);
        Question question = questionReader.getByIdAndRoomId(questionId, room.getId());
        permissionValidator.validateAnswerCreatePermission(room.getId(), member.getId(), question);
        Answer answer = AnswerMapper.toEntity(question, member, request);

        return answerRepository.save(answer);

    }

    @Transactional
    public AnswerGetResponse getAnswer(Long roomId, Long questionId, Long memberId) {
        Member member = memberReader.getById(memberId);
        roomReader.getById(roomId);
        questionReader.getByIdAndRoomId(questionId, roomId);
        Answer answer = answerReader.getByQuestionId(questionId);

        Long emojiCount = answer.getEmojiCount();
        boolean isEmojied = emojiReader.findByMemberIdAndTargetIdAndTargetType(member.getId(),
            answer.getId(), TargetType.ANSWER).isPresent();

        return AnswerMapper.toAnswerGetResponse(answer, emojiCount, isEmojied);
    }

    @Transactional
    public Answer update(Long answerId, Long memberId, AnswerRequest request) {
        Answer answer = permissionValidator.validateAnswerUpdatePermission(answerId, memberId);

        answer.updataContent(request.content());

        return answer;
    }

    @Transactional
    public void delete(Long answerId, Long memberId) {
        Answer answer = permissionValidator.validateAnswerDeletePermission(answerId, memberId);
        answerRepository.delete(answer);
    }

    @Transactional
    public void deleteByQuestion(Long questionId) {
        answerRepository.deleteByQuestionId(questionId);
    }

    @Transactional
    public void deleteByQuestionList(List<Question> questions) {
        answerRepository.deleteByQuestionIn(questions);
    }
}
