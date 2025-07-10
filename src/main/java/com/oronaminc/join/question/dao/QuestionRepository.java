package com.oronaminc.join.question.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.oronaminc.join.question.domain.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("""
        select q.room.id, count(q)
        from Question q
        where q.room.id  in (:roomIds)
        group by q.room.id
        """)
    List<Object[]> countByRoomIds(List<Long> roomIds);

    List<Question> findByRoomId(Long roomId);

    void deleteByRoomId(Long roomId);
}
