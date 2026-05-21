package com.example.olca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record ChatMessgeCreateRequset(

        @NotNull(message = "세션 ID는 필수입니다")
        Long sessionId,

        @NotBlank(message = "질문은 필수 입니다")
        String question,
        @NotBlank(message = "답변은 필수 입니다")
        String answer
) {

}
