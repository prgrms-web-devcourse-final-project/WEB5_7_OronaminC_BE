package com.oronaminc.join.answer.api;

import com.oronaminc.join.answer.dto.AnswerGetResponse;
import com.oronaminc.join.answer.service.AnswerService;
import com.oronaminc.join.member.security.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @Operation(
        summary = "답변 조회",
        description = "답변 보기 클릭 시 질문에 대한 답변을 조회",
        responses = {
            @ApiResponse(responseCode = "200", description = "답변 조회 성공"),
            @ApiResponse(responseCode = "400", description = "답변이 없는데 답변보기 버튼이 활성화 되어 잘못된 조회 접근")

        }
    )
    @GetMapping("/rooms/{roomId}/questions/{questionId}/answers")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AnswerGetResponse> getAnswer(
        @PathVariable Long roomId,
        @PathVariable Long questionId,
        @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();
        AnswerGetResponse response = answerService.getAnswer(roomId, questionId, memberId);
        return ResponseEntity.ok(response);
    }

}
