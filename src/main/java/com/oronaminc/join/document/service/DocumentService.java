package com.oronaminc.join.document.service;


import com.oronaminc.join.document.event.DocumentCreateEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oronaminc.join.document.dao.DocumentRepository;
import com.oronaminc.join.document.dto.DocumentRequest;
import com.oronaminc.join.document.dto.DocumentResponse;
import com.oronaminc.join.document.mapper.DocumentMapper;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.infra.service.S3Service;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.room.domain.Room;

import lombok.RequiredArgsConstructor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final S3Service s3Service;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public void deleteByRoomId(Long roomId) {
        documentRepository.deleteByRoomId(roomId);
    }

    public DocumentResponse generatePresignedUrl(DocumentRequest request, String memberRole) {
        if (!memberRole.equals(MemberType.MEMBER.name())) {
            throw new ErrorException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        String OriginalFileName = request.fileName();
        String extension = "";

        int dotIndex = OriginalFileName.lastIndexOf('.');
        if (dotIndex != -1) {
            extension = OriginalFileName.substring(dotIndex);
        }

        String uuid = UUID.randomUUID().toString();
        String objectKey = "temp/" + uuid + extension;
        String presignedUrl = s3Service.generatePresignedUrl(objectKey);

        return new DocumentResponse(presignedUrl, objectKey);
    }

    @Transactional
    public void saveDocument(String objectKey, Room room) {
        String fileName = objectKey.replaceAll("^.*/","");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        String oldKey = "temp/" + encodedFileName;
        String newKey = "documents/" + encodedFileName;

        documentRepository.save(DocumentMapper.toDocument(newKey, fileName, room));
        publisher.publishEvent(new DocumentCreateEvent(oldKey, fileName));
    }

}
