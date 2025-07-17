package com.oronaminc.join.answer.util;

import static com.oronaminc.join.global.exception.ErrorCode.UNAUTHORIZED_DELETE_ANSWER;
import static com.oronaminc.join.global.exception.ErrorCode.UNAUTHORIZED_EDIT_ANSWER;
import static com.oronaminc.join.global.exception.ErrorCode.UNAUTHORIZED_ROLE_ANSWER;

import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.service.AnswerReader;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.participant.service.ParticipantReader;
import com.oronaminc.join.question.domain.Question;
import com.oronaminc.join.room.domain.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionValidator {

    private final ParticipantReader participantReader;
    private final AnswerReader answerReader;

    public void validateAnswerPermission(Long roomId, Long memberId) {
        // TODO: 생성시 조건 ( null 이면 안됨, " " 안됨, 팀원이나 발표자, 질문 작성자 본인만 생성가능 )
        Participant participant = participantReader.getByRoomIdAndMemberId(roomId, memberId);
        ParticipantType type = participant.getParticipantType();

        if (type == ParticipantType.GUEST) {
            throw new ErrorException(UNAUTHORIZED_ROLE_ANSWER);
        }
    }

    public void validateAnswerUpdatePermission(Long answerId, Long memberId) {
        Answer answer = answerReader.getById(answerId);

        if (!answer.getMember().getId().equals(memberId)) {
            throw new ErrorException(UNAUTHORIZED_EDIT_ANSWER);
        }
    }

    public void validateAnswerDeletePermission(Long answerId, Long memberId) {
        Answer answer = answerReader.getById(answerId);

        Room room = answer.getQuestion().getRoom();
        Question question = answer.getQuestion();

        Participant participant = participantReader.getByRoomIdAndMemberId(room.getId(), memberId);
        ParticipantType type = participant.getParticipantType();

        boolean isQuestionWriter = question.getMember().getId().equals(memberId);

        if (!(isQuestionWriter || type == ParticipantType.TEAM
            || type == ParticipantType.PRESENTER)) {
            throw new ErrorException(UNAUTHORIZED_DELETE_ANSWER);
        }

    }

}
