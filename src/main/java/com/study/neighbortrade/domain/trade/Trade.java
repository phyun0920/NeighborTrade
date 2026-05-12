package com.study.neighbortrade.domain.trade;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.product.ProductPost;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trade {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "product_post_id", nullable = false)
    private ProductPost productPost;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "buyer_id", nullable = false)
    private Member buyer;

    @Enumerated(EnumType.STRING)

    @Column(nullable = false, length = 30)
    private TradeStatus status;
    private LocalDateTime completedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = TradeStatus.REQUESTED;
    }
    public void accept() {
        if (status != TradeStatus.REQUESTED) {
            throw new IllegalStateException("요청중인 거래만 수락할 수 있습니다.");
        }
        status = TradeStatus.ACCEPTED;
    }
    public void complete() {
        if (status != TradeStatus.ACCEPTED) {
            throw new IllegalStateException("예약중인 거래만 완료할 수 있습니다.");
        }
        status = TradeStatus.COMPLETED;
        completedAt = LocalDateTime.now();
    }
    public void cancel() {
        if (status == TradeStatus.COMPLETED) {
            throw new IllegalStateException("완료된 거래는 취소할 수 없습니다.");
        }
        if (status == TradeStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 거래입니다.");
        }
        status = TradeStatus.CANCELLED;
    }
    public boolean isParticipant(Member member) {
        return member != null && (seller.getId().equals(member.getId()) || buyer.getId().equals(member.getId()));
    }
}
