package com.oronaminc.join.answer.api;

import com.oronaminc.join.answer.dto.AnswerGetResponse;
import com.oronaminc.join.answer.service.AnswerService;
import com.oronaminc.join.member.security.MemberDetails;
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
