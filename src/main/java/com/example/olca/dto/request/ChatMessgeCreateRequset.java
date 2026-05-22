package com.example.olca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record ChatMessgeCreateRequset (

        @NotNull(message = "세션 ID는 필수입니다")
        Long sessionId,

        @NotBlank(message = "질문은 필수입니다")
        @Size(max = 1500, message = "질문은 1500자 이내로 입력해주세요")
        String question,
        @NotBlank(message = "답변은 필수입니다")
        String answer
) {

}
