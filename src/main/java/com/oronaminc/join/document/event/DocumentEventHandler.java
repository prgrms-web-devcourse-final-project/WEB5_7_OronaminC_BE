package com.oronaminc.join.document.event;


import com.oronaminc.join.global.exception.ErrorCode;
import com.oronaminc.join.global.exception.ErrorException;
import com.oronaminc.join.infra.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


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

            log.debug("✅ S3 파일 이동 성공: {} → {}", event.objectKey(), newKey);
        } catch (Exception e) {
            log.error("❌ S3 파일 이동 실패: {} → {}, 이유: {}", event.objectKey(), newKey, e.getMessage(), e);
            throw new ErrorException(ErrorCode.MOVEMENT_FILE_FAILED);
        }
    }
}
