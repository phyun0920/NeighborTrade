package com.study.neighbortrade.repository;

import com.study.neighbortrade.domain.community.CommunityComment;
import com.study.neighbortrade.domain.community.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    List<CommunityComment> findByPostOrderByCreatedAtAsc(CommunityPost post);
}
