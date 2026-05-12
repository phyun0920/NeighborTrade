package com.study.neighbortrade.dto.product;

import com.study.neighbortrade.domain.product.ProductCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductPostRequestDto {

    @NotBlank

    @Size(max = 120)
    private String title;

    @NotNull
    private ProductCategory category;

    @Min(0)
    private int price;
    private boolean giveaway;

    @NotBlank
    private String content;

    @Size(max = 500)
    private String representativeImageUrl;

    @Size(max = 120)
    private String tradePlace;
}
