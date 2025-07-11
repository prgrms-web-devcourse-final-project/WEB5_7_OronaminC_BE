package com.oronaminc.join.document.service;


import com.oronaminc.join.document.dto.DocumentRequest;
import com.oronaminc.join.document.dto.DocumentResponse;
import com.oronaminc.join.document.mapper.DocumentMapper;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.infra.service.S3Service;
import com.oronaminc.join.member.domain.MemberType;
import static com.oronaminc.join.global.exception.ErrorCode.*;

import com.oronaminc.join.room.domain.Room;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.oronaminc.join.document.dao.DocumentRepository;
import com.oronaminc.join.document.domain.Document;

import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final S3Service s3Service;

    public Document getDocumentByRoomId(Long roomId) {
        return documentRepository.findByRoomId(roomId)
                .orElseThrow(() -> new ErrorException(NOT_FOUND_FILE));
    }

    public void deleteByRoomId(Long roomId) {
        documentRepository.deleteByRoomId(roomId);
    }

    public DocumentResponse generatePresignedUrl(DocumentRequest request, String memberRole) {
        if (!memberRole.equals(MemberType.MEMBER.name())) {
            throw new ErrorException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        String uuid = UUID.randomUUID().toString();
        String objectKey = "documents/" + uuid + "_" + request.fileName();
        String presignedUrl = s3Service.generatePresignedUrl(objectKey);

        return new DocumentResponse(presignedUrl, objectKey);
    }

    @Transactional
    public void saveDocument(String objectKey, Room room) {
        String fileName = objectKey.replaceAll("^.*/","");

        documentRepository.save(DocumentMapper.toDocument(objectKey, fileName, room));
    }

}
