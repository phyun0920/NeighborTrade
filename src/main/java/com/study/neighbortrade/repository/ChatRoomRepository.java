package com.study.neighbortrade.repository;

import com.study.neighbortrade.domain.chat.ChatRoom;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.product.ProductPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByProductPostAndBuyer(ProductPost productPost, Member buyer);

    List<ChatRoom> findBySellerOrBuyerOrderByUpdatedAtDesc(Member seller, Member buyer);
}
