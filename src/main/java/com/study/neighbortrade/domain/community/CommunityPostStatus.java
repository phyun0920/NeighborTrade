package com.study.neighbortrade.domain.community;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommunityPostStatus {
    VISIBLE("노출"),
    HIDDEN("숨김");

    private final String label;
}
