package com.study.neighbortrade.web;

import com.study.neighbortrade.domain.community.CommunityPost;
import com.study.neighbortrade.domain.product.ProductPost;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Thymeleaf {@code @relativeTime} — 상대 시간 표시.
 * <p>
 * Phase 3 Step 3(U9): 중고 카드·상세 「N분 전」「끌올 N분 전」 (20260609).<br>
 * Phase 3 Step 6(COM): {@link #communityMeta} — 동네생활 카드·상세 (20260611).
 */
@Component("relativeTime")
public class RelativeTimeFormatter {

    public String format(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        Duration elapsed = Duration.between(time, LocalDateTime.now());
        long minutes = elapsed.toMinutes();
        if (minutes < 1) {
            return "방금 전";
        }
        if (minutes < 60) {
            return minutes + "분 전";
        }
        long hours = elapsed.toHours();
        if (hours < 24) {
            return hours + "시간 전";
        }
        long days = elapsed.toDays();
        if (days < 7) {
            return days + "일 전";
        }
        return time.toLocalDate().toString();
    }

    public String productMeta(ProductPost post) {
        if (post == null) {
            return "";
        }
        String timeLabel = format(post.getActiveAt());
        return post.isBumped() ? "끌올 " + timeLabel : timeLabel;
    }

    /**
     * 동네생활 카드·상세 메타 시간.
     * <p>
     * Phase 3 Step 6(COM): {@code community-card}·{@code community-detail-v2}에서 사용 (20260611).<br>
     * 기준: {@link CommunityPost#getCreatedAt()} — 끌올 없음.
     */
    public String communityMeta(CommunityPost post) {
        if (post == null) {
            return "";
        }
        return format(post.getCreatedAt());
    }
}
