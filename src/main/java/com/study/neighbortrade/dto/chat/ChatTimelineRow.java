package com.study.neighbortrade.dto.chat;

import com.study.neighbortrade.domain.chat.ChatMessage;
import lombok.Getter;

/** 20260518월 채팅방 타임라인 — 날짜 구분선 또는 메시지 한 줄 */
@Getter
public final class ChatTimelineRow {
    private final boolean dateDivider;
    private final String dateLabel;
    private final ChatMessage message;

    private ChatTimelineRow(boolean dateDivider, String dateLabel, ChatMessage message) {
        this.dateDivider = dateDivider;
        this.dateLabel = dateLabel;
        this.message = message;
    }

    public static ChatTimelineRow dateDivider(String label) {
        return new ChatTimelineRow(true, label, null);
    }

    public static ChatTimelineRow message(ChatMessage message) {
        return new ChatTimelineRow(false, null, message);
    }
}
