package com.oronaminc.join.websocket.session;

import static org.assertj.core.api.Assertions.*;

import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * CurrentParticipantManager.addParticipant() 검증.
 * 방 상세 조회(getRoomParticipants) 없이 join이 먼저 들어오는 경우(서버 재기동 직후 재연결 등)에도
 * 참가자가 등록되어야 하고, 인원 제한은 동시 입장에서도 지켜져야 한다.
 */
@DisplayName("CurrentParticipantManager 참가자 등록 검증")
class CurrentParticipantManagerTest {

    private static final Long ROOM_ID = 1L;

    private final CurrentParticipantManager manager = new CurrentParticipantManager();

    @Test
    @DisplayName("방이 아직 등록되지 않은 상태에서 첫 참가자를 추가할 수 있다")
    void addParticipant_toUnregisteredRoom() {
        // when
        manager.addParticipant(ROOM_ID, 100L, 10);

        // then
        assertThat(manager.getRoomParticipants(ROOM_ID)).containsExactly(100L);
    }

    @Test
    @DisplayName("이미 참가한 회원을 다시 추가해도 인원 수는 늘지 않는다")
    void addParticipant_duplicateIgnored() {
        // given
        manager.addParticipant(ROOM_ID, 100L, 10);

        // when
        manager.addParticipant(ROOM_ID, 100L, 10);

        // then
        assertThat(manager.getRoomParticipants(ROOM_ID)).hasSize(1);
    }

    @Test
    @DisplayName("인원 제한을 넘으면 UNAUTHORIZED_LIMIT_PARTICIPANT 예외가 발생하고 기존 참가자는 유지된다")
    void addParticipant_overLimit() {
        // given
        manager.addParticipant(ROOM_ID, 100L, 1);

        // when & then
        assertThatThrownBy(() -> manager.addParticipant(ROOM_ID, 200L, 1))
            .isInstanceOf(ErrorException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.UNAUTHORIZED_LIMIT_PARTICIPANT);
        assertThat(manager.getRoomParticipants(ROOM_ID)).containsExactly(100L);
    }

    @Test
    @DisplayName("동시에 입장해도 인원 제한만큼만 등록된다")
    void addParticipant_concurrentRespectsLimit() throws InterruptedException {
        // given
        int limit = 10;
        int threads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger rejected = new AtomicInteger();

        // when
        for (long memberId = 1; memberId <= threads; memberId++) {
            long id = memberId;
            executor.submit(() -> {
                try {
                    start.await();
                    manager.addParticipant(ROOM_ID, id, limit);
                } catch (ErrorException e) {
                    rejected.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();

        // then
        assertThat(manager.getRoomParticipants(ROOM_ID)).hasSize(limit);
        assertThat(rejected).hasValue(threads - limit);
    }
}
