package com.oronaminc.join.member.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTests {

    @Test
    @DisplayName("닉네임을 수정하면 정상적으로 변경된다.")
    void updateNickname_test() {

        // given
        Member member = Member.builder().nickname("min").build();
        String newNickname = "newNickname";

        // when
        member.updateNickname(newNickname);

        // then
        assertThat(member.getNickname()).isEqualTo(newNickname);

    }

}