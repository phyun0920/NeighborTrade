package com.study.neighbortrade.domain.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportStatus {
    RECEIVED("접수"),
    REVIEWING("검토중"),
    RESOLVED("처리완료"),
    REJECTED("반려");

    private final String label;
}
