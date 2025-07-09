package com.oronaminc.join.document.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oronaminc.join.document.domain.Document;

public interface DocumentJpaRepository extends JpaRepository<Document, Long> {

}
