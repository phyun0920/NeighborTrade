package com.study.neighbortrade.repository;

import com.study.neighbortrade.domain.chat.ChatMessage;
import com.study.neighbortrade.domain.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);
}
