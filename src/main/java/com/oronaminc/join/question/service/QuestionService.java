package com.oronaminc.join.question.service;

import static com.oronaminc.join.global.exception.ErrorCode.NOT_FOUND_PARTICIPANT;

import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.global.util.SliceUtil;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.domain.QuestionSort;
import com.oronaminc.join.question.dto.QuestionCreateRequest;
import com.oronaminc.join.question.dto.QuestionListResponse;
import com.oronaminc.join.question.util.QuestionMapper;
import com.oronaminc.join.question.dao.QuestionRepository;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.dao.RoomRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final ParticipantRepository participantRepository;

    @Transactional
    public Question create(Long roomId, Long memberId, QuestionCreateRequest requestDto) {

        Member member = getMember(memberId);

        Room room = getRoom(roomId);

        if (!participantRepository.existsByRoomIdAndMemberId(room.getId(), member.getId())) {
            throw new ErrorException(NOT_FOUND_PARTICIPANT);
        }

        Question question = QuestionMapper.toQuestion(room, member, requestDto);

        questionRepository.save(question);

        return question;
    }

    @Transactional(readOnly = true)
    public Slice<QuestionListResponse> getQuestions(
        QuestionSort sort,
        Long lastId,
        Long lastEmojiCount,
        int size,
        Long memberId
    ) {
        getMember(memberId);
        Pageable pageable = PageRequest.of(0, size + 1);

        List<QuestionListResponse> questions = switch (sort) {
            case CREATEDAT -> questionRepository.findByCreatedAt(lastId,
                    memberId, pageable);
            case EMOJI -> questionRepository.findByEmojiCount(lastId,
                    lastEmojiCount, memberId, pageable);
            case MYQUESTION -> questionRepository.findByMyQuestion(lastId,
                    memberId, pageable);
        };

        return SliceUtil.toSlice(questions, PageRequest.of(0, size));
    }

    private Room getRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_ROOM));
        return room;
    }

    private Member getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_MEMBER));
        return member;
    }
}
