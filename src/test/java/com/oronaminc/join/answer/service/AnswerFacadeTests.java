package com.oronaminc.join.answer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.oronaminc.join.answer.domain.Answer;
import com.oronaminc.join.answer.dto.AnswerRequest;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;

@ExtendWith(MockitoExtension.class)
class AnswerFacadeTests {

    @InjectMocks
    private AnswerFacade answerFacade;

    @Mock
    private AnswerService answerService;

    private final AnswerRequest request = new AnswerRequest("수정된 내용");

    @Test
    @DisplayName("답변 수정 - 버전 충돌이 나면 재시도 후 성공한다")
    void update_retrySuccess_afterOptimisticLockConflict() {
        // given
        Answer answer = Answer.builder().id(200L).content("수정된 내용").build();
        given(answerService.update(1L, 200L, 1L, request))
            .willThrow(new ObjectOptimisticLockingFailureException(Answer.class, 200L))
            .willThrow(new ObjectOptimisticLockingFailureException(Answer.class, 200L))
            .willReturn(answer);

        // when
        Answer result = answerFacade.update(1L, 200L, 1L, request);

        // then
        assertThat(result).isSameAs(answer);
        verify(answerService, times(3)).update(1L, 200L, 1L, request);
    }

    @Test
    @DisplayName("답변 수정 - 재시도를 모두 소진하면 CONFLICT_ANSWER 예외가 발생한다")
    void update_fail_whenRetriesExhausted() {
        // given
        given(answerService.update(1L, 200L, 1L, request))
            .willThrow(new ObjectOptimisticLockingFailureException(Answer.class, 200L));

        // when & then
        assertThatThrownBy(() -> answerFacade.update(1L, 200L, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT_ANSWER);
        verify(answerService, times(10)).update(1L, 200L, 1L, request);
    }

    @Test
    @DisplayName("답변 수정 - 버전 충돌이 아닌 예외는 재시도하지 않고 그대로 전파한다")
    void update_noRetry_onOtherException() {
        // given
        given(answerService.update(1L, 200L, 1L, request))
            .willThrow(new ErrorException(ErrorCode.UNAUTHORIZED_EDIT_ANSWER));

        // when & then
        assertThatThrownBy(() -> answerFacade.update(1L, 200L, 1L, request))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_EDIT_ANSWER);
        verify(answerService, times(1)).update(1L, 200L, 1L, request);
    }

    @Test
    @DisplayName("답변 삭제 - 버전 충돌이 나면 재시도 후 성공한다")
    void delete_retrySuccess_afterOptimisticLockConflict() {
        // given
        willThrow(new ObjectOptimisticLockingFailureException(Answer.class, 200L))
            .willThrow(new ObjectOptimisticLockingFailureException(Answer.class, 200L))
            .willDoNothing()
            .given(answerService).delete(1L, 200L, 1L);

        // when & then
        assertThatCode(() -> answerFacade.delete(1L, 200L, 1L)).doesNotThrowAnyException();
        verify(answerService, times(3)).delete(1L, 200L, 1L);
    }

    @Test
    @DisplayName("답변 삭제 - 재시도를 모두 소진하면 CONFLICT_ANSWER 예외가 발생한다")
    void delete_fail_whenRetriesExhausted() {
        // given
        willThrow(new ObjectOptimisticLockingFailureException(Answer.class, 200L))
            .given(answerService).delete(1L, 200L, 1L);

        // when & then
        assertThatThrownBy(() -> answerFacade.delete(1L, 200L, 1L))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT_ANSWER);
        verify(answerService, times(10)).delete(1L, 200L, 1L);
    }

    @Test
    @DisplayName("답변 삭제 - 버전 충돌이 아닌 예외는 재시도하지 않고 그대로 전파한다")
    void delete_noRetry_onOtherException() {
        // given
        willThrow(new ErrorException(ErrorCode.UNAUTHORIZED_DELETE_ANSWER))
            .given(answerService).delete(1L, 200L, 1L);

        // when & then
        assertThatThrownBy(() -> answerFacade.delete(1L, 200L, 1L))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_DELETE_ANSWER);
        verify(answerService, times(1)).delete(1L, 200L, 1L);
    }
}
