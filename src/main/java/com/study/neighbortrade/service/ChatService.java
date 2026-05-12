package com.study.neighbortrade.service;

import com.study.neighbortrade.domain.chat.ChatMessage;
import com.study.neighbortrade.domain.chat.ChatRoom;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.product.ProductPost;
import com.study.neighbortrade.dto.chat.ChatMessageRequestDto;
import com.study.neighbortrade.repository.ChatMessageRepository;
import com.study.neighbortrade.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProductPostService productPostService;

    @Transactional
    public ChatRoom findOrCreateRoom(Long productPostId, Member buyer) {
        ProductPost post = productPostService.findById(productPostId);
        if (post.isSeller(buyer)) throw new IllegalArgumentException("본인 판매글에는 채팅할 수 없습니다.");
        return chatRoomRepository.findByProductPostAndBuyer(post, buyer)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.builder()
                        .productPost(post)
                        .seller(post.getSeller())
                        .buyer(buyer)
                        .build()));
    }

    public List<ChatRoom> myRooms(Member member) {
        return chatRoomRepository.findBySellerOrBuyerOrderByUpdatedAtDesc(member, member);
    }

    public ChatRoom findRoom(Long roomId, Member member) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        if (!room.isParticipant(member)) throw new IllegalArgumentException("채팅방 참여자만 접근할 수 있습니다.");
        return room;
    }

    public List<ChatMessage> messages(ChatRoom room) {
        return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(room);
    }

    @Transactional
    public ChatMessage saveMessage(Long roomId, Member sender, ChatMessageRequestDto dto) {
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new IllegalArgumentException("메시지를 입력해 주세요.");
        }
        ChatRoom room = findRoom(roomId, sender);
        return chatMessageRepository.save(ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content(dto.getContent().trim())
                .build());
    }
}
