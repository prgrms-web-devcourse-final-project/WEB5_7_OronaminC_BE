package com.oronaminc.join.question.dao;

import com.oronaminc.join.question.domain.QuestionSort;
import com.oronaminc.join.question.dto.QuestionFlatResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface QuestionCustomRepository {

    List<QuestionFlatResponse> findQuestionsOrderBy(Long lastId, Long lastEmojiCount,
        Long memberId, Long roomId, QuestionSort sortType, Pageable pageable);
}
