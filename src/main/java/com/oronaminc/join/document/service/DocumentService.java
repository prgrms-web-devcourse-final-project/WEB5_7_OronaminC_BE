package com.oronaminc.join.document.service;

import static com.oronaminc.join.global.exception.ErrorCode.*;

import org.springframework.stereotype.Service;

import com.oronaminc.join.document.dao.DocumentRepository;
import com.oronaminc.join.document.domain.Document;
import com.oronaminc.join.global.exception.ErrorException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;

    public Document getDocumentByRoomId(Long roomId) {
        return documentRepository.findByRoomId(roomId)
                .orElseThrow(() -> new ErrorException(NOT_FOUND_FILE));
    }

    public void deleteByRoomId(Long roomId) {
        documentRepository.deleteByRoomId(roomId);
    }
}
