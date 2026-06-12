package com.study.neighbortrade.domain.product;

public enum MarketView {
    GRID("grid", "그리드"),
    LIST("list", "리스트");

    private final String param;
    private final String label;

    MarketView(String param, String label) {
        this.param = param;
        this.label = label;
    }

    public String getParam() {
        return param;
    }

    public String getLabel() {
        return label;
    }

    public static MarketView fromParam(String param) {
        if (param == null || param.isBlank()) {
            return GRID;
        }
        for (MarketView value : values()) {
            if (value.param.equals(param)) {
                return value;
            }
        }
        return GRID;
    }
}
