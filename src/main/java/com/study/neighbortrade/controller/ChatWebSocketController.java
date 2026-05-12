package com.study.neighbortrade.controller;

import com.study.neighbortrade.domain.chat.ChatMessage;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.dto.chat.ChatMessageRequestDto;
import com.study.neighbortrade.dto.chat.ChatMessageResponseDto;
import com.study.neighbortrade.service.ChatService;
import com.study.neighbortrade.service.CurrentMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final ChatService chatService;
    private final CurrentMemberService currentMemberService;

    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessageResponseDto send(@DestinationVariable Long roomId, ChatMessageRequestDto dto, Principal principal) {
        Member sender = currentMemberService.require(principal);
        ChatMessage message = chatService.saveMessage(roomId, sender, dto);
        return ChatMessageResponseDto.from(message);
    }
}
