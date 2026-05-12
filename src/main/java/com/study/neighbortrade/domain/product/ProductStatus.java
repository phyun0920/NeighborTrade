package com.study.neighbortrade.domain.product;

public enum ProductStatus {
    ON_SALE("판매중"), RESERVED("예약중"), COMPLETED("거래완료"), HIDDEN("숨김");
    private final String label;
    ProductStatus(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
