package com.oronaminc.join.room.dto;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
        @NotBlank
        @Length(min = 3, max = 20)
        String title,

        @NotBlank
        @Length(max = 150)
        String description,

        @FutureOrPresent
        LocalDate endDate,

        @Max(50)
        Integer participantLimit,

        @NotBlank
        String documentUrl,

        @NotNull
        @Size(max = 5)
        List<String> teamEmail
) {
}
