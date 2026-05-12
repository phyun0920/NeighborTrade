package com.study.neighbortrade.domain.trade;

public enum TradeStatus {
    REQUESTED("요청중"), ACCEPTED("예약중"), COMPLETED("거래완료"), CANCELLED("취소");
    private final String label;
    TradeStatus(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
