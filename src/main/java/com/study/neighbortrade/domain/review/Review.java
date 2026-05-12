package com.study.neighbortrade.domain.review;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.trade.Trade;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "trade_id", nullable = false, unique = true)
    private Trade trade;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "reviewer_id", nullable = false)
    private Member reviewer;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "reviewee_id", nullable = false)
    private Member reviewee;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
