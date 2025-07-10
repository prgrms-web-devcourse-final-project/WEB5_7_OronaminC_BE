package com.oronaminc.join.document.service;

import com.oronaminc.join.document.dto.DocumentRequest;
import com.oronaminc.join.document.dto.DocumentResponse;
import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.infra.service.S3Service;
import com.oronaminc.join.member.domain.MemberType;
import org.springframework.stereotype.Service;

import com.oronaminc.join.document.dao.DocumentJpaRepository;

import lombok.RequiredArgsConstructor;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentJpaRepository documentJpaRepository;
    private final S3Service s3Service;

    public DocumentResponse generatePresignedUrl(DocumentRequest request, String memberRole) {
        if (!memberRole.equals(MemberType.MEMBER.name())) {
            throw new ErrorException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        String objectKey = request.fileName();
        String presignedUrl = s3Service.generateUploadPresignedUrl(objectKey);

        return new DocumentResponse(presignedUrl);
    }

    // 전체 URL에서 ObjectKey 추출
    private String extractObjectKey(String documentUrl) {
        String path = URI.create(documentUrl).getPath();

        return path.startsWith("/") ? path.substring(1) : path;
    }
}
