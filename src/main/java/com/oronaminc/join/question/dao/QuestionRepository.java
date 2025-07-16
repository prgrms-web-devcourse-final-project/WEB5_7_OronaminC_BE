package com.oronaminc.join.question.dao;

import com.oronaminc.join.question.domain.Question;
import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long>, QuestionCustomRepository {

    Optional<Question> findByIdAndRoomId(Long questionId, Long roomId);

    @Query("""
        select q.room.id, count(q)
        from Question q
        where q.room.id  in (:roomIds)
        group by q.room.id
    """)
    List<Object[]> countByRoomIds(List<Long> roomIds);

    List<Question> findByRoomId(Long roomId);

    void deleteById(Long questionId);

    void deleteByRoomId(Long roomId);

    Long countByRoomId(@Param("roomId") Long roomId);

    @Query ("""
        select q
        from Question q
        where q.room.id = :roomId
        order by q.emojiCount desc
    """)
    List<Question> findTop3QuestionByRoomId(Long roomId, Pageable pageable);
}
