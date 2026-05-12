package com.study.neighbortrade.service;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.review.Review;
import com.study.neighbortrade.domain.trade.*;
import com.study.neighbortrade.dto.review.ReviewRequestDto;
import com.study.neighbortrade.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final TradeRepository tradeRepository;
    private final MemberRepository memberRepository;
    public List<Review> findReceived(Member member) {
        return reviewRepository.findByRevieweeOrderByCreatedAtDesc(member);
    }
    public Trade findReviewableTrade(Long tradeId, Member reviewer) {
        Trade trade = tradeRepository.findById(tradeId).orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다."));
        if (!trade.isParticipant(reviewer)) throw new IllegalArgumentException("거래 당사자만 후기 작성이 가능합니다.");
        if (trade.getStatus() != TradeStatus.COMPLETED) throw new IllegalArgumentException("거래완료 후 후기 작성이 가능합니다.");
        if (reviewRepository.existsByTrade(trade)) throw new IllegalArgumentException("이미 후기를 작성했습니다.");
        return trade;
    }

    @Transactional
    public void create(Long tradeId, Member reviewer, ReviewRequestDto dto) {
        Trade trade = findReviewableTrade(tradeId, reviewer);
        Member reviewee = trade.getSeller().getId().equals(reviewer.getId()) ? trade.getBuyer() : trade.getSeller();
        reviewRepository.save(Review.builder().trade(trade).reviewer(reviewer).reviewee(reviewee).rating(dto.getRating()).content(dto.getContent()).build());
        double adjusted = Math.max(0, Math.min(50, reviewee.getMannerScore() + (dto.getRating() - 3) * 0.5));
        reviewee.updateMannerScore(Math.round(adjusted * 10) / 10.0);
        memberRepository.save(reviewee);
    }
}
