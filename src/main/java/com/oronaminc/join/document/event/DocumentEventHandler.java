package com.oronaminc.join.document.event;


import com.oronaminc.join.infra.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentEventHandler {

    private final S3Service s3Service;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDocumentEvent(DocumentCreateEvent event) {
        String newKey = "documents/" + event.fileName();

        try {
            s3Service.moveObject(event.objectKey(), newKey);

            log.info("✅ S3 파일 이동 성공: {} → {}", event.objectKey(), newKey);
        } catch (Exception e) {
            log.info("원본 키: {}", event.objectKey());
            log.info("디코딩 키: {}", URLDecoder.decode(event.objectKey(), StandardCharsets.UTF_8));
            log.info("❌ S3 파일 이동 실패: {} → {}, 이유: {}", event.objectKey(), newKey, e.getMessage(), e);
        }
    }
}
