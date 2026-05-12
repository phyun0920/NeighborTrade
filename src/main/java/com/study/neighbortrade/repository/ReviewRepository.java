package com.study.neighbortrade.repository;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.review.Review;
import com.study.neighbortrade.domain.trade.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByTrade(Trade trade);
    List<Review> findByRevieweeOrderByCreatedAtDesc(Member reviewee);
}
