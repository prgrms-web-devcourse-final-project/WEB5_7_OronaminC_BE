package com.oronaminc.join.question.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oronaminc.join.answer.service.AnswerService;
import com.oronaminc.join.global.util.SliceUtil;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.service.MemberReader;
import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.question.dao.QuestionRepository;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.domain.QuestionSort;
import com.oronaminc.join.question.dto.QuestionAssembleResponse;
import com.oronaminc.join.question.dto.QuestionCreateRequest;
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
    private final AnswerService answerService;
    private final RoomReader roomReader;
    private final QuestionReader questionReader;
    private final MemberReader memberReader;
    private final ParticipantService participantService;

    @Transactional
    public Question create(Long roomId, Long memberId, QuestionCreateRequest requestDto) {

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

        List<QuestionFlatResponse> questions = switch (sort) {
            case QuestionSort.CREATEDAT -> questionReader.findByCreatedAt(lastId,
                memberId, roomId, pageable);
            case QuestionSort.EMOJI -> questionReader.findByEmojiCount(lastId,
                lastEmojiCount, memberId, roomId, pageable);
            case QuestionSort.MYQUESTION -> questionReader.findByMyQuestion(lastId,
                memberId, roomId, pageable);
        };

        List<QuestionAssembleResponse> assembledList = questions.stream()
            .map(QuestionMapper::toQuestionListResponse).toList();

        return SliceUtil.toSlice(assembledList, PageRequest.of(0, size));
    }


    @Transactional
    public void deleteByRoomId(Long roomId) {
        List<Question> questions = questionReader.findByRoomId(roomId);
        answerService.deleteByQuestionList(questions);
        questionRepository.deleteByRoomId(roomId);
    }

}
