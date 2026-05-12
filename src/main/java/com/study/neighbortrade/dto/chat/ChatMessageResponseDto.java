package com.study.neighbortrade.dto.chat;

import com.study.neighbortrade.domain.chat.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageResponseDto {
    private Long id;
    private Long senderId;
    private String senderNickname;
    private String content;
    private String createdAt;

    public static ChatMessageResponseDto from(ChatMessage message) {
        return new ChatMessageResponseDto(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getContent(),
                message.getCreatedAt().toString()
        );
    }
}
