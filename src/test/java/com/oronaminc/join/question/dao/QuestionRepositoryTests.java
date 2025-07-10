package com.oronaminc.join.question.dao;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.oronaminc.join.question.dto.QuestionFlatResponse;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = "/question-test-data.sql")
class QuestionRepositoryTests {

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    @DisplayName("최신순 정렬이 성공적으로 동작한다")
    void findByCreatedAt_success() {
        // given
        Pageable pageable = PageRequest.of(0, 5);
        Long memberId = 1L;
        Long roomId = 1L;

        // when
        List<QuestionFlatResponse> result = questionRepository.findByCreatedAt(
            null, memberId, roomId, pageable
        );

        // then
        assertThat(result).hasSize(5);
        assertThat(result.get(0).questionId()).isGreaterThan(result.get(4).questionId());

    }

    @Test
    @DisplayName("공감순 정렬이 성공적으로 동작한다")
    void findByEmoji_success() {
        // given
        Pageable pageable = PageRequest.of(0, 5);
        Long memberId = 1L;
        Long roomId = 1L;

        // when
        List<QuestionFlatResponse> result = questionRepository.findByEmojiCount(
            null, null, memberId, roomId, pageable
        );

        // then
        assertThat(result).hasSize(5);
        assertThat(result).isSortedAccordingTo(
            Comparator.comparing(QuestionFlatResponse::emojiCount).reversed());

    }

    @Test
    @DisplayName("내 질문 정렬이 성공적으로 동작한다")
    void findByMyQuestion_success() {
        // given
        Pageable pageable = PageRequest.of(0, 5);
        Long memberId = 1L;
        Long roomId = 1L;

        // when
        List<QuestionFlatResponse> result = questionRepository.findByMyQuestion(
            null, memberId, roomId, pageable
        );

        // then
        assertThat(result).hasSize(5);
        assertThat(result).allMatch(q -> q.memberId().equals(memberId));

    }
}