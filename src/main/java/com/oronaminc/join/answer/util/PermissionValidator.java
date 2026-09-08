package com.oronaminc.join.answer.util;

import static com.oronaminc.join.global.exception.ErrorCode.UNAUTHORIZED_EDIT_ANSWER;

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

    public void validateAnswerCreatePermission(Long roomId, Long memberId, Question question) {
        validatePermission(roomId, memberId, question, PermissionType.CREATE);
    }

    public Answer validateAnswerUpdatePermission(Long answerId, Long memberId) {
        Answer answer = answerReader.getById(answerId);

        if (!answer.getMember().getId().equals(memberId)) {
            throw new ErrorException(UNAUTHORIZED_EDIT_ANSWER);
        }

        return answer;
    }

    public Answer validateAnswerDeletePermission(Long answerId, Long memberId) {
        Answer answer = answerReader.getById(answerId);
        Room room = answer.getQuestion().getRoom();
        Question question = answer.getQuestion();

        validatePermission(room.getId(), memberId, question, PermissionType.DELETE);

        return answer;
    }

    private void validatePermission(Long roomId, Long memberId, Question question,
        PermissionType permissionType) {
        Participant participant = participantReader.getByRoomIdAndMemberId(roomId, memberId);
        ParticipantType type = participant.getParticipantType();
        boolean isQuestionWriter = question.getMember().getId().equals(memberId);

        if (!(isQuestionWriter || type == ParticipantType.TEAM
            || type == ParticipantType.PRESENTER)) {
            throw new ErrorException(permissionType.toErrorCode());
        }
    }

}
