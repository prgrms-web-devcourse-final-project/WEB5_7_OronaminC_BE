package com.oronaminc.join.document.api;


import com.oronaminc.join.document.dto.DocumentRequest;
import com.oronaminc.join.document.dto.DocumentResponse;
import com.oronaminc.join.document.service.DocumentService;
import com.oronaminc.join.member.security.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    @Operation(
            summary = "Presigned URL 발급",
            description = "클라이언트가 S3에 파일을 직접 업로드 할 수 있도록 Presigned URL을 발급합니다.\n" +
                          "요청 시 파일명, 파일 타입, 파일 크기 정보를 받고, 업로드용 URL을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Presigned URL 발급 성공"),
                    @ApiResponse(responseCode = "400", description = "파일 형식 오류 또는 파일 최대 크기 초과"),
                    @ApiResponse(responseCode = "403", description = "회원 권한이 없는 접근")
            }
    )
    @PostMapping("/presigned-url")
    @ResponseStatus(HttpStatus.OK)
    public DocumentResponse generatePresignedUrl(
            @Valid @RequestBody DocumentRequest documentRequest,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        String memberRole = memberDetails.getRole();
        return documentService.generatePresignedUrl(documentRequest, memberRole);
    }
}
