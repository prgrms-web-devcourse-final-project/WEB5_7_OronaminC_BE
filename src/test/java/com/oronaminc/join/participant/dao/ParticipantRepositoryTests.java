package com.oronaminc.join.participant.dao;

import com.oronaminc.join.member.dao.MemberRepository;
import com.oronaminc.join.member.domain.Member;
import com.oronaminc.join.member.dto.ParticipantCountDto;
import com.oronaminc.join.participant.domain.Participant;
import com.oronaminc.join.participant.domain.ParticipantType;
import com.oronaminc.join.question.dao.QuestionRepository;
import com.oronaminc.join.room.dao.RoomRepository;
import com.oronaminc.join.room.domain.Room;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ParticipantRepositoryTests {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoomRepository roomRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder().build());
        Member otherMember = memberRepository.save(Member.builder().build());

        Room room1 = roomRepository.save(Room.builder().build());
        Room room2 = roomRepository.save(Room.builder().build());
        Room room3 = roomRepository.save(Room.builder().build());

        participantRepository.save(Participant.builder()
            .room(room1)
            .member(member)
            .participantType(ParticipantType.PRESENTER)
            .build()
        );
        participantRepository.save(Participant.builder()
            .room(room2)
            .member(member)
            .participantType(ParticipantType.TEAM)
            .build()
        );
        participantRepository.save(Participant.builder()
            .room(room3)
            .member(member)
            .participantType(ParticipantType.GUEST)
            .build()
        );
        participantRepository.save(Participant.builder()
            .room(room3)
            .member(otherMember)
            .participantType(ParticipantType.GUEST)
            .build()
        );
    }
    
    @Test
    @DisplayName("멤버ID로 생성한방, 참여한방 수를 조회한다.")
    void countByMemberIdGroupByParticipantType_test(){
    	// given

        // when
        List<ParticipantCountDto> result = participantRepository.countByMemberIdGroupByParticipantType(
            member.getId());

        // then
        assertThat(result.getFirst().count()).isEqualTo(1);
        assertThat(result.get(1).count()).isEqualTo(1);
        assertThat(result.get(2).count()).isEqualTo(1);

    }

    @Test
    @DisplayName("해당 멤버가 생성/참가한 모든 방이 조회된다.")
    void findByMemberId_test(){

    	// given
        Pageable pageable = PageRequest.of(0, 10);

    	// when
        Page<Participant> result = participantRepository.findByMemberId(member.getId(),
            pageable);

        // then
        assertThat(result.getContent().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("해당 멤버가 생성한 모든 방이 조회된다.")
    void findByMemberIdAndParticipantType_test(){

    	// given
        Pageable pageable = PageRequest.of(0, 10);

    	// when
        Page<Participant> result = participantRepository.findByMemberIdAndParticipantType(member.getId(),
            ParticipantType.PRESENTER, pageable);

        // then
        assertThat(result.getContent().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("해당 멤버가 참여한 모든 방이 조회된다.")
    void findByMemberIdAndParticipantTypeNot_test(){

    	// given
        Pageable pageable = PageRequest.of(0, 10);

    	// when
        Page<Participant> result = participantRepository.findByMemberIdAndParticipantTypeNot(member.getId(),
            ParticipantType.PRESENTER, pageable);

        // then
        assertThat(result.getContent().size()).isEqualTo(2);
    }

}