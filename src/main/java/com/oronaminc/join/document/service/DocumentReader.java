package com.oronaminc.join.document.service;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.oronaminc.join.document.dao.DocumentRepository;
import com.oronaminc.join.document.domain.Document;
import com.oronaminc.join.global.exception.ErrorException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DocumentReader {
    private final DocumentRepository documentRepository;

    public Optional<Document> findByRoomId(Long id) {
        return documentRepository.findByRoomId(id);
    }

    public Document getByRoomId(Long roomId) {
        return findByRoomId(roomId)
                .orElseThrow(() -> new ErrorException(NOT_FOUND_FILE));
    }
}
