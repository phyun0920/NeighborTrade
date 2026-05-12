package com.study.neighbortrade.repository;

import com.study.neighbortrade.domain.community.CommunityPost;
import com.study.neighbortrade.domain.community.CommunityPostStatus;
import com.study.neighbortrade.domain.location.Neighborhood;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    Page<CommunityPost> findByNeighborhoodAndStatusOrderByCreatedAtDesc(Neighborhood neighborhood, CommunityPostStatus status, Pageable pageable);

    long countByStatus(CommunityPostStatus status);
}
