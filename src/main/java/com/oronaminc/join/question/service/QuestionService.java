package com.oronaminc.join.question.service;

import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.repository.MemberRepository;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.question.dto.QuestionRequestDto;
import com.oronaminc.join.question.mapper.QuestionMapper;
import com.oronaminc.join.question.repository.QuestionRepository;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;

    public Long create(Long roomId, Long memberId, QuestionRequestDto requestDto) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_MEMBER));

        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_ROOM));

        Question question = QuestionMapper.toQuestion(room, member, requestDto);

        questionRepository.save(question);

        return question.getId();
    }

}
