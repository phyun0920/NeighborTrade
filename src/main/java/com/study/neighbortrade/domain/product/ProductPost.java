package com.study.neighbortrade.domain.product;

import com.study.neighbortrade.domain.location.Neighborhood;
import com.study.neighbortrade.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_post")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductPost {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "neighborhood_id", nullable = false)
    private Neighborhood neighborhood;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private boolean giveaway;

    @Enumerated(EnumType.STRING)

    @Column(nullable = false, length = 30)
    private ProductCategory category;

    @Column(length = 500)
    private String representativeImageUrl;

    @Column(length = 120)
    private String tradePlace;

    @Enumerated(EnumType.STRING)

    @Column(nullable = false, length = 30)
    private ProductStatus status;

    @Column(nullable = false)
    private long viewCount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /** Phase 3 Step 3(U9): 끌올 시각 — null 이면 미끌올 (20260609) */
    private LocalDateTime bumpedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = ProductStatus.ON_SALE;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    public void update(String title, ProductCategory category, int price, boolean giveaway, String content, String imageUrl, String tradePlace) {
        this.title = title;
        this.category = category;
        this.price = price;
        this.giveaway = giveaway;
        this.content = content;
        this.representativeImageUrl = normalize(imageUrl);
        this.tradePlace = tradePlace;
    }
    public void changeStatus(ProductStatus status) {
        this.status = status;
    }
    public void hide() {
        this.status = ProductStatus.HIDDEN;
    }
    public void increaseViewCount() {
        this.viewCount++;
    }

    /** 목록 최신순·상대시간 기준 시각 */
    public LocalDateTime getActiveAt() {
        return bumpedAt != null ? bumpedAt : createdAt;
    }

    public boolean isBumped() {
        return bumpedAt != null;
    }

    public void bump() {
        this.bumpedAt = LocalDateTime.now();
    }
    public boolean isSeller(Member member) {
        return member != null && seller.getId().equals(member.getId());
    }
    public String getDisplayImageUrl() {
        return representativeImageUrl == null || representativeImageUrl.isBlank() ? "/images/placeholder-product.svg" : representativeImageUrl;
    }
    private String normalize(String url) {
        return url == null || url.isBlank() ? null : url.trim();
    }
}
