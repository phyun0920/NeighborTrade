package com.study.neighbortrade.service;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.product.ProductFavorite;
import com.study.neighbortrade.domain.product.ProductPost;
import com.study.neighbortrade.domain.product.ProductStatus;
import com.study.neighbortrade.repository.ProductFavoriteRepository;
import com.study.neighbortrade.repository.ProductPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductFavoriteService {
    private final ProductFavoriteRepository productFavoriteRepository;
    private final ProductPostRepository productPostRepository;

    @Transactional
    public boolean toggle(Member member, Long postId) {
        ProductPost post = productPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("판매글을 찾을 수 없습니다."));
        if (post.getStatus() == ProductStatus.HIDDEN) {
            throw new IllegalArgumentException("찜할 수 없는 게시글입니다.");
        }
        if (productFavoriteRepository.existsByMemberAndProductPost(member, post)) {
            productFavoriteRepository.deleteByMemberAndProductPost(member, post);
            return false;
        }
        productFavoriteRepository.save(ProductFavorite.builder()
                .member(member)
                .productPost(post)
                .build());
        return true;
    }

    public Page<ProductPost> findMyFavorites(Member member, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        return productFavoriteRepository.findVisibleFavoritesByMember(member, pageable);
    }

    public Set<Long> findFavoritedPostIds(Member member, Collection<Long> postIds) {
        if (member == null || postIds == null || postIds.isEmpty()) {
            return Set.of();
        }
        return productFavoriteRepository.findFavoritedPostIds(member, postIds);
    }
}
