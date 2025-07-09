package com.oronaminc.join.document.service;

import org.springframework.stereotype.Service;

import com.oronaminc.join.document.dao.DocumentJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentJpaRepository documentJpaRepository;

}
