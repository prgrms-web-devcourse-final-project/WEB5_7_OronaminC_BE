package com.oronaminc.join.document.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public record DocumentRequest(

        @NotBlank
        String fileName,

        @NotBlank
        @Pattern(regexp = "application/pdf", message = "파일 형식이 올바르지 않습니다.")
        String fileType,

        @NotNull
        @Max(value = 5_242_880, message = "파일 용량이 5MB를 초과하였습니다.")
        Long fileSize
) { }
