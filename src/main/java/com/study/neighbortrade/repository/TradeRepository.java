package com.study.neighbortrade.repository;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.product.ProductPost;
import com.study.neighbortrade.domain.trade.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findBySellerOrBuyerOrderByCreatedAtDesc(Member seller, Member buyer);
    Optional<Trade> findByProductPostAndBuyer(ProductPost productPost, Member buyer);
}
