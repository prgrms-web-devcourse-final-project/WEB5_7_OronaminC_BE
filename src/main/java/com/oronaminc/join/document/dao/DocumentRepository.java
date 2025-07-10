package com.oronaminc.join.document.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oronaminc.join.document.domain.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByRoomId(Long roomId);
}
