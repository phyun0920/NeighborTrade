package com.study.neighbortrade.service;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.product.*;
import com.study.neighbortrade.domain.trade.*;
import com.study.neighbortrade.repository.ProductPostRepository;
import com.study.neighbortrade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeService {
    private final TradeRepository tradeRepository;
    private final ProductPostRepository productPostRepository;
    public List<Trade> findMyTrades(Member member) {
        return tradeRepository.findBySellerOrBuyerOrderByCreatedAtDesc(member, member);
    }
    public Trade findForMember(Long id, Member member) {
        Trade trade = tradeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다."));
        if (!trade.isParticipant(member)) throw new IllegalArgumentException("거래 당사자만 볼 수 있습니다.");
        return trade;
    }

    @Transactional
    public void request(Long postId, Member buyer) {
        ProductPost post = productPostRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("판매글을 찾을 수 없습니다."));
        if (!buyer.isLocalVerified()) throw new IllegalArgumentException("동네 인증 후 거래 요청할 수 있습니다.");
        if (post.isSeller(buyer)) throw new IllegalArgumentException("본인 판매글에는 거래 요청할 수 없습니다.");
        if (post.getStatus() != ProductStatus.ON_SALE) throw new IllegalArgumentException("판매중인 글만 거래 요청할 수 있습니다.");
        tradeRepository.findByProductPostAndBuyer(post, buyer).ifPresent(t -> {
            throw new IllegalArgumentException("이미 거래 요청한 판매글입니다.");
        }
        );
        post.changeStatus(ProductStatus.RESERVED);
        tradeRepository.save(Trade.builder().productPost(post).seller(post.getSeller()).buyer(buyer).status(TradeStatus.REQUESTED).build());
    }

    @Transactional
    public void accept(Long tradeId, Member seller) {
        Trade trade = findForMember(tradeId, seller);
        if (!trade.getSeller().getId().equals(seller.getId())) throw new IllegalArgumentException("판매자만 수락할 수 있습니다.");
        trade.accept();
        trade.getProductPost().changeStatus(ProductStatus.RESERVED);
    }

    @Transactional
    public void complete(Long tradeId, Member member) {
        Trade trade = findForMember(tradeId, member);
        if (!trade.getSeller().getId().equals(member.getId())) throw new IllegalArgumentException("판매자만 거래 완료 처리할 수 있습니다.");
        trade.complete();
        trade.getProductPost().changeStatus(ProductStatus.COMPLETED);
    }

    @Transactional
    public void cancel(Long tradeId, Member member) {
        Trade trade = findForMember(tradeId, member);
        trade.cancel();
        trade.getProductPost().changeStatus(ProductStatus.ON_SALE);
    }
}
