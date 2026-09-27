package com.oronaminc.join.question.dao;

import static com.oronaminc.join.answer.domain.QAnswer.answer;
import static com.oronaminc.join.emoji.domain.QEmoji.emoji;
import static com.oronaminc.join.member.domain.QMember.member;
import static com.oronaminc.join.question.domain.QQuestion.question;

import com.oronaminc.join.question.domain.QuestionSort;
import com.oronaminc.join.question.dto.QuestionFlatResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class QuestionCustomRepositoryImpl implements QuestionCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<QuestionFlatResponse> findQuestionsOrderBy(Long lastId, Long lastEmojiCount,
        Long memberId, Long roomId, QuestionSort sortType, Pageable pageable) {

        Predicate where = createPredicate(lastId, lastEmojiCount, memberId, roomId,
            sortType);

        JPAQuery<QuestionFlatResponse> query = jpaQueryFactory
            .select(Projections.constructor(QuestionFlatResponse.class,
                question.id,
                question.content,
                question.emojiCount,
                JPAExpressions.selectOne().from(answer).where(answer.question.eq(question))
                    .exists(),
                JPAExpressions.selectOne().from(emoji).where(emoji.member.id.eq(memberId)).exists(),
                member.id,
                member.nickname,
                question.createdAt
            ))
            .from(question)
            .join(question.member, member)
            .where(where)
            .orderBy(createOrderSpecifiers(sortType));

        return query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    private Predicate createPredicate(Long lastId, Long lastEmojiCount,
        Long memberId, Long roomId, QuestionSort sortType) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(question.room.id.eq(roomId));

        switch (sortType) {
            case CREATEDAT -> {
                if (lastId != null) {
                    builder.and(question.id.lt(lastId));
                }
            }
            case EMOJI -> {
                if (lastEmojiCount != null) {
                    builder.and(
                        question.emojiCount.lt(lastEmojiCount)
                            .or(question.emojiCount.eq(lastEmojiCount).and(question.id.lt(lastId)))
                    );
                }
            }
            case MYQUESTION -> {
                builder.and(question.member.id.eq(memberId));
                if (lastId != null) {
                    builder.and(question.id.lt(lastId));
                }
            }
        }

        return builder;
    }

    private OrderSpecifier<?>[] createOrderSpecifiers(QuestionSort sortType) {

        List<OrderSpecifier<?>> orders = new ArrayList<>();

        // 공감순인 경우에만 emojiCount 정렬 조건 추가
        if (sortType.equals(QuestionSort.EMOJI)) {
            orders.add(new OrderSpecifier<>(Order.DESC, question.emojiCount));
        }

        // 최신순, 내 질문
        orders.add(new OrderSpecifier<>(Order.DESC, question.id));

        return orders.toArray(new OrderSpecifier[0]);
    }
}
