package com.study.neighbortrade.domain.search;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "popular_search_keyword")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularSearchKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer rank;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private Integer searchCount;

    @Column(nullable = false)
    private LocalDate period;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /** 검색 횟수 증가 */
    public void incrementCount() {
        this.searchCount++;
    }

    /** 키워드 업데이트 (admin) */
    public void updateKeyword(String keyword) {
        this.keyword = keyword;
    }

    /** rank 변경 (admin) */
    public void updateRank(Integer rank) {
        this.rank = rank;
    }

    /** 정적 팩토리 메서드 */
    public static PopularSearchKeyword of(Integer rank, String keyword, Integer searchCount, LocalDate period) {
        return PopularSearchKeyword.builder()
                .rank(rank)
                .keyword(keyword)
                .searchCount(searchCount)
                .period(period)
                .build();
    }
}
