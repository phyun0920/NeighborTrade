package com.study.neighbortrade.dto.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityPostRequestDto {
    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    private String content;
}
