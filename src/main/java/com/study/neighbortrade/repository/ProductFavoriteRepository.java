package com.study.neighbortrade.repository;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.product.ProductFavorite;
import com.study.neighbortrade.domain.product.ProductPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;

public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, Long> {
    boolean existsByMemberAndProductPost(Member member, ProductPost productPost);

    void deleteByMemberAndProductPost(Member member, ProductPost productPost);

    @Query("""
            SELECT f.productPost
            FROM ProductFavorite f
            WHERE f.member = :member
              AND f.productPost.status <> 'HIDDEN'
            ORDER BY f.createdAt DESC
            """)
    Page<ProductPost> findVisibleFavoritesByMember(
            @Param("member") Member member,
            Pageable pageable
    );

    @Query("""
            SELECT f.productPost.id
            FROM ProductFavorite f
            WHERE f.member = :member
              AND f.productPost.id IN :postIds
            """)
    Set<Long> findFavoritedPostIds(
            @Param("member") Member member,
            @Param("postIds") Collection<Long> postIds
    );
}
