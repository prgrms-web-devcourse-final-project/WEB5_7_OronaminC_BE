package com.oronaminc.join.answer.service;

import java.util.function.Supplier;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerRequest;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnswerFacade {

    private static final int MAX_RETRY = 10;
    private static final long RETRY_INTERVAL_MS = 50;

    private final AnswerService answerService;

    public Answer update(Long roomId, Long answerId, Long memberId, AnswerRequest request) {
        return retryOnOptimisticLock(() -> answerService.update(roomId, answerId, memberId, request));
    }

    public void delete(Long roomId, Long answerId, Long memberId) {
        retryOnOptimisticLock(() -> {
            answerService.delete(roomId, answerId, memberId);
            return null;
        });
    }

    // 공감 수 변경도 Answer의 version을 올리므로, 수정·삭제 중 공감이 들어오면 커밋 시점에 버전 충돌이 난다.
    // 트랜잭션 바깥에서 재시도해 매번 새 트랜잭션으로 재조회 후 다시 반영한다.
    private <T> T retryOnOptimisticLock(Supplier<T> action) {
        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                return action.get();
            } catch (ObjectOptimisticLockingFailureException e) {
                try {
                    Thread.sleep(RETRY_INTERVAL_MS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new ErrorException(ErrorCode.CONFLICT_ANSWER);
                }
            }
        }
        throw new ErrorException(ErrorCode.CONFLICT_ANSWER);
    }

}
