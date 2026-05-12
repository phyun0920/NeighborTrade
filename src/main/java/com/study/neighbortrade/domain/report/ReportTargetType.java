package com.study.neighbortrade.domain.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportTargetType {
    PRODUCT_POST("판매글"),
    COMMUNITY_POST("동네생활 글"),
    COMMUNITY_COMMENT("동네생활 댓글"),
    CHAT_MESSAGE("채팅 메시지");

    private final String label;
}
