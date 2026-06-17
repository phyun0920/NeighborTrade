package com.study.neighbortrade.domain.search;

import com.study.neighbortrade.domain.location.Neighborhood;
import com.study.neighbortrade.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_v2_log")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false, updatable = false)
    private LocalDateTime searchedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "browsing_neighborhood_id")
    private Neighborhood browsingNeighborhood;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private int productCount;

    @PrePersist
    void onCreate() {
        if (searchedAt == null) {
            searchedAt = LocalDateTime.now();
        }
    }

    /** 정적 팩토리 메서드 */
    public static SearchLog of(String keyword, Neighborhood browsingNeighborhood, Member member, int productCount) {
        return SearchLog.builder()
                .keyword(keyword)
                .browsingNeighborhood(browsingNeighborhood)
                .member(member)
                .productCount(productCount)
                .build();
    }
}
