package com.oronaminc.join.emoji.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.oronaminc.join.answer.service.AnswerReader;
import com.oronaminc.join.emoji.dao.EmojiRepository;
import com.oronaminc.join.emoji.domain.Emoji;
import com.oronaminc.join.emoji.domain.TargetType;
import com.oronaminc.join.emoji.dto.EmojiRequest;
import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.service.MemberReader;
import com.oronaminc.join.question.service.QuestionReader;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.domain.RoomStatus;
import com.oronaminc.join.room.service.RoomReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Import({EmojiFacade.class, EmojiService.class, MemberReader.class, EmojiReader.class,
    RoomReader.class, QuestionReader.class, AnswerReader.class})
@ActiveProfiles("test")
class EmojiFacadeTests {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmojiRepository emojiRepository;

    @Autowired
    private EmojiFacade emojiFacade;


    @Test
    @DisplayName("동시에 50개의 공감 생성 요청")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void createEmoji_success_test() throws InterruptedException {

        Long emojiCount = 0L;
        // given
        Room savedRoom = roomRepository.saveAndFlush(
            Room.builder()
                .title("제목")
                .description("내용")
                .secretCode("123456")
                .emojiCount(emojiCount)
                .participantLimit(0)
                .endedAt(LocalDateTime.now())
                .version(0)
                .roomStatus(RoomStatus.STARTED)
                .build()
        );
        Long roomId = savedRoom.getId();

        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<Member> members = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Member member = Member.builder().build();
            members.add(memberRepository.saveAndFlush(member));
        }

        // when
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executorService.submit(() -> {
                try {
                    emojiFacade.createEmoji(members.get(idx).getId(),
                        new EmojiRequest(TargetType.ROOM, roomId));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Room findRoom = roomRepository.findById(roomId).orElse(null);
        assertThat(findRoom.getEmojiCount()).isEqualTo(threadCount);
        assertThat(findRoom.getVersion()).isEqualTo(threadCount);

    }

    @Test
    @DisplayName("동시에 50개의 공감 삭제 요청")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deleteEmoji_success_test() throws InterruptedException {

        Long emojiCount = 50L;

        // given
        Room savedRoom = roomRepository.saveAndFlush(
            Room.builder()
                .title("제목")
                .description("내용")
                .secretCode("123456")
                .emojiCount(emojiCount)
                .participantLimit(0)
                .endedAt(LocalDateTime.now())
                .version(0)
                .roomStatus(RoomStatus.STARTED)
                .build()
        );
        Long roomId = savedRoom.getId();

        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<Member> members = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Member member = Member.builder().build();
            members.add(memberRepository.saveAndFlush(member));
            emojiRepository.saveAndFlush(Emoji.create(member, TargetType.ROOM, roomId));
        }

        // when
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executorService.submit(() -> {
                try {
                    emojiFacade.deleteEmoji(members.get(idx).getId(),
                        new EmojiRequest(TargetType.ROOM, roomId));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Room findRoom = roomRepository.findById(roomId).orElse(null);
        assertThat(findRoom.getEmojiCount()).isEqualTo(0);
        assertThat(findRoom.getVersion()).isEqualTo(threadCount);

    }

}