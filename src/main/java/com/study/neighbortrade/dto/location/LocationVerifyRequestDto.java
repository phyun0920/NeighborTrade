package com.study.neighbortrade.dto.location;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationVerifyRequestDto {

    @NotNull
    private Long neighborhoodId;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}
