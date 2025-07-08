package com.oronaminc.join.question.dao;

import com.oronaminc.join.question.domain.Question;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("""
        select q.room.id, count(q)
        from Question q
        where q.room.id  in (:roomIds)
        group by q.room.id
        """)
    Map<Long, Long> countByRoomIds(List<Long> roomIds);
}
