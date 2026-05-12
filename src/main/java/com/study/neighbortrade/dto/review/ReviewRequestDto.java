package com.study.neighbortrade.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequestDto {

    @Min(1)

    @Max(5)
    private int rating;

    @NotBlank
    private String content;
}
