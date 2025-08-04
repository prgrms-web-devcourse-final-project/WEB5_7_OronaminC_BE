package com.oronaminc.join.room.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.participant.dao.ParticipantRepository;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;
import com.oronaminc.join.room.domain.RoomStatus;
import com.oronaminc.join.room.domain.RoomType;
import com.oronaminc.join.room.dto.RoomUpdateStatusRequest;

@SpringBootTest
@ActiveProfiles("test")
@EnableCaching
class RoomCacheTests {
    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomReader roomReader;

    @MockitoSpyBean
    private RoomRepository roomRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @BeforeEach
    void setUp() {
        Room room = Room.builder()
                .title("Test Room")
                .description("Test Description")
                .roomStatus(RoomStatus.BEFORE_START)
                .roomType(RoomType.PUBLIC)
                .secretCode("123456")
                .build();

        roomRepository.save(room);

        Member member = Member.builder()
                .build();

        memberRepository.save(member);

        Participant participant = Participant.builder()
                .room(room)
                .member(member)
                .participantType(ParticipantType.PRESENTER)
                .build();

        participantRepository.save(participant);
    }

    @Test
    void 캐시가_적용되어_두번째_조회는_DB_접근이_없어야_한다() {
        Long roomId = 1L;

        Room room = roomReader.getCacheById(roomId);

        int repeat = 10;
        for (int count = 0; count < repeat; count++) {
            Room cacheRoom = roomReader.getCacheById(roomId);
            assertThat(room).isSameAs(cacheRoom);
        }

        verify(roomRepository, times(1)).findById(roomId);

        Cache roomCache = cacheManager.getCache("roomById");
        Room cached = roomCache.get(roomId, Room.class);
        assertThat(cached).isNotNull();
    }

    @Test
    void updateRoomStatus_호출시_캐시가_삭제되어야_한다() {
        Long roomId = 1L;
        Long memberId = 1L;

        roomReader.getCacheById(roomId);

        Room cachedBefore = cacheManager.getCache("roomById").get(roomId, Room.class);
        assertThat(cachedBefore).isNotNull();

        roomService.updateRoomStatus(memberId, roomId, new RoomUpdateStatusRequest(RoomStatus.STARTED));

        Room cachedAfter = cacheManager.getCache("roomById").get(roomId, Room.class);
        assertThat(cachedAfter).isNull();
    }
}