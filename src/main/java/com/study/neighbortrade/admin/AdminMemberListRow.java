package com.study.neighbortrade.admin;

import java.time.LocalDateTime;

/** 관리자 회원 목록용 행(이메일은 마스킹된 값만 포함). */
public record AdminMemberListRow(
        Long id,
        String username,
        String nickname,
        String emailMasked,
        String roleLabel,
        String loginTypeLabel,
        String providerSummary,
        boolean localVerified,
        String verifiedNeighborhoodLabel,
        double mannerScore,
        LocalDateTime createdAt) {}
