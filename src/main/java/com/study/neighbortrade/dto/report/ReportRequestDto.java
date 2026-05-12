package com.study.neighbortrade.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequestDto {
    @NotBlank
    @Size(max = 100)
    private String reason;

    @Size(max = 1000)
    private String detail;
}
