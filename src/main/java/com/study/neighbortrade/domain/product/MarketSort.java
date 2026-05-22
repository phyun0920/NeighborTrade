package com.study.neighbortrade.domain.product;

import org.springframework.data.domain.Sort;

public enum MarketSort {
    LATEST("latest", "최신순", Sort.by("createdAt").descending()),
    PRICE_ASC("priceAsc", "가격 낮은순", Sort.by("price").ascending().and(Sort.by("createdAt").descending())),
    PRICE_DESC("priceDesc", "가격 높은순", Sort.by("price").descending().and(Sort.by("createdAt").descending()));

    private final String param;
    private final String label;
    private final Sort sort;

    MarketSort(String param, String label, Sort sort) {
        this.param = param;
        this.label = label;
        this.sort = sort;
    }

    public String getParam() {
        return param;
    }

    public String getLabel() {
        return label;
    }

    public Sort toSort() {
        return sort;
    }

    public static MarketSort fromParam(String param) {
        if (param == null || param.isBlank()) {
            return LATEST;
        }
        for (MarketSort value : values()) {
            if (value.param.equals(param)) {
                return value;
            }
        }
        return LATEST;
    }
}
