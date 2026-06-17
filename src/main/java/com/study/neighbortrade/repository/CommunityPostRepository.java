package com.study.neighbortrade.repository;

import com.study.neighbortrade.domain.community.CommunityPost;
import com.study.neighbortrade.domain.community.CommunityPostStatus;
import com.study.neighbortrade.domain.location.Neighborhood;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    // Step 2(C1): browsing 동네 필터 (20260609)
    Page<CommunityPost> findByNeighborhoodAndStatusOrderByCreatedAtDesc(Neighborhood neighborhood, CommunityPostStatus status, Pageable pageable);

    // Phase 3 Step 2(C1): browsing 없을 때 전체 공개 글 (20260609)
    Page<CommunityPost> findByStatusOrderByCreatedAtDesc(CommunityPostStatus status, Pageable pageable);

    // Phase 3 Step 6(COM): search band 키워드 — 동네+제목 (20260611)
    Page<CommunityPost> findByNeighborhoodAndStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            Neighborhood neighborhood, CommunityPostStatus status, String keyword, Pageable pageable);

    // Phase 3 Step 6(COM): search band 키워드 — 전체+제목 (20260611)
    Page<CommunityPost> findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            CommunityPostStatus status, String keyword, Pageable pageable);

    long countByStatus(CommunityPostStatus status);
}
