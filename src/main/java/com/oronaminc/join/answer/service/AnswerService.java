package com.oronaminc.join.answer.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oronaminc.join.answer.dao.AnswerRepository;
import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerGetResponse;
import com.oronaminc.join.answer.dto.AnswerRequest;
import com.oronaminc.join.answer.mapper.AnswerMapper;
import com.oronaminc.join.answer.util.PermissionValidator;
import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.emoji.service.EmojiReader;
import com.oronaminc.join.global.util.SliceUtil;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.service.MemberReader;
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

    @Transactional(readOnly = true)
    public Slice<AnswerGetResponse> getAnswers(
        Long roomId,
        Long questionId,
        Long memberId,
        Long lastId,
        LocalDateTime lastCreatedAt,
        int size
    ) {
        memberReader.getById(memberId);
        roomReader.getById(roomId);
        questionReader.getByIdAndRoomId(questionId, roomId);

        Pageable pageable = PageRequest.of(0, size + 1);

        List<Answer> answers = (lastCreatedAt == null || lastId == null)
            ? answerReader.getFirstPageByQuestionId(questionId, pageable)
            : answerReader.getAnswerByQuestionIdWithCursor(questionId, lastCreatedAt, lastId,
                pageable);

        // 공감 여부 일괄 조회
        List<Long> answerIds = answers.stream().map(Answer::getId).toList();

        Set<Long> emojiedAnswerIds = memberId != null
            ? emojiReader.findTargetIdsByMemberAndTargetTypeInBatch(memberId, TargetType.ANSWER, answerIds)
            : Set.of();

        List<AnswerGetResponse> responseList = answers.stream()
            .map(answer -> {
                boolean isEmojied = emojiedAnswerIds.contains(answer.getId());
                return AnswerMapper.toAnswerGetResponse(answer, isEmojied);
            })
            .toList();

        return SliceUtil.toSlice(responseList, PageRequest.of(0, size));

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
