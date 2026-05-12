package com.study.neighbortrade.dto.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityCommentRequestDto {
    @NotBlank
    @Size(max = 500)
    private String content;
}
