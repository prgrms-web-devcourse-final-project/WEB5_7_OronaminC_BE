package com.oronaminc.join.document.service;

import com.oronaminc.join.document.domain.Document;
import com.oronaminc.join.document.dto.*;
import com.oronaminc.join.participant.service.ParticipantService;
import com.oronaminc.join.room.service.RoomReader;
import org.springframework.stereotype.Service;

import com.oronaminc.join.document.dao.DocumentRepository;
import com.oronaminc.join.document.mapper.DocumentMapper;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.infra.service.S3Service;
import com.oronaminc.join.member.domain.MemberType;
import com.oronaminc.join.room.domain.Room;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class DocumentService {

    private final S3Service s3Service;
    private final RoomReader roomReader;
    private final DocumentReader documentReader;
    private final DocumentRepository documentRepository;
    private final ParticipantService participantService;

    public void deleteByRoomId(Long roomId) {
        documentRepository.deleteByRoomId(roomId);
    }

    // 발표자료 생성, 조회, 수정시 사용
    public DocumentS3UploadResponse generateUploadPresignedUrl(DocumentS3UploadRequest request, String memberRole) {
        if (!memberRole.equals(MemberType.MEMBER.name())) {
            throw new ErrorException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        String uuid = UUID.randomUUID().toString();
        String objectKey = "documents/" + uuid + "_" + request.fileName();
        String presignedUrl = s3Service.generatePresignedUrl(objectKey);

        return new DocumentS3UploadResponse(presignedUrl, objectKey);
    }

    @Transactional
    public DocumentCreateResponse saveDocument(Long roomId, DocumentCreateRequest request, String memberRole) {
        if (!memberRole.equals(MemberType.MEMBER.name())) {
            throw new ErrorException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        Room room = roomReader.getById(roomId);
        String fileName = request.documentUrl().replaceAll("^.*/","");

        documentRepository.save(DocumentMapper.toDocument(request.documentUrl(), fileName, room));
        return new DocumentCreateResponse(request.documentUrl());
    }

    @Transactional(readOnly = true)
    public DocumentGetResponse getDocument(Long roomId) {
        String documentUrl = documentReader.getByRoomId(roomId).getFileUrl();

        return new DocumentGetResponse(s3Service.generatePresignedUrl(documentUrl));
    }

    @Transactional
    public void updateDocument(Long roomId, DocumentUpdateRequest request, Long memberId) {
        // 권한 검증
        participantService.validatePresenter(memberId, roomId);

        Document document = documentReader.getByRoomId(roomId);
        s3Service.deleteFile(document.getFileUrl());

        document.update(request.documentUrl());
    }

    @Transactional
    public void deleteDocument(Long roomId, Long memberId) {
        // 권한 검증
        participantService.validatePresenter(memberId, roomId);

        Document document = documentReader.getByRoomId(roomId);

        s3Service.deleteFile(document.getFileUrl());
        documentRepository.deleteByRoomId(roomId);
    }

}
