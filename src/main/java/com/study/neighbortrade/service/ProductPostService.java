package com.study.neighbortrade.service;

import com.study.neighbortrade.config.MarketProperties;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.product.MarketSort;
import com.study.neighbortrade.domain.product.ProductCategory;
import com.study.neighbortrade.domain.product.ProductPost;
import com.study.neighbortrade.domain.product.ProductStatus;
import com.study.neighbortrade.dto.product.ProductPostRequestDto;
import com.study.neighbortrade.repository.ProductPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductPostService {
    private final ProductPostRepository productPostRepository;
    private final ProductImageService productImageService;
    private final MarketProperties marketProperties;
    public Page<ProductPost> list(
            String keyword,
            boolean onlyOnSale,
            ProductCategory category,
            Long neighborhoodId,
            MarketSort sort,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        MarketSort resolvedSort = sort != null ? sort : MarketSort.LATEST;
        Pageable pageable = PageRequest.of(safePage, safeSize);
        if (resolvedSort == MarketSort.LATEST) {
            return productPostRepository.searchVisibleWithFiltersLatest(
                    keyword, category, neighborhoodId, onlyOnSale, pageable);
        }
        return productPostRepository.searchVisibleWithFilters(
                keyword, category, neighborhoodId, onlyOnSale,
                PageRequest.of(safePage, safeSize, resolvedSort.toSort()));
    }

    @Transactional
    public ProductPost findDetail(Long id) {
        ProductPost post = findById(id);
        post.increaseViewCount();
        return post;
    }
    public ProductPost findById(Long id) {
        return productPostRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("판매글을 찾을 수 없습니다."));
    }

    @Transactional
    public ProductPost create(Member seller, ProductPostRequestDto dto, List<MultipartFile> imageFiles) {
        requireLocalVerified(seller);
        ProductPost post = productPostRepository.save(ProductPost.builder().seller(seller).neighborhood(seller.getVerifiedNeighborhood()).title(dto.getTitle()).category(dto.getCategory()).price(dto.isGiveaway() ? 0 : dto.getPrice()).giveaway(dto.isGiveaway()).content(dto.getContent()).representativeImageUrl(dto.getRepresentativeImageUrl()).tradePlace(dto.getTradePlace()).status(ProductStatus.ON_SALE).build());
        String representativeUrl = productImageService.replaceImages(post, imageFiles);
        post.update(dto.getTitle(), dto.getCategory(), dto.isGiveaway() ? 0 : dto.getPrice(), dto.isGiveaway(), dto.getContent(), representativeUrl, dto.getTradePlace());
        return post;
    }

    @Transactional
    public void update(
            Long id,
            Member seller,
            ProductPostRequestDto dto,
            List<MultipartFile> imageFiles,
            List<Long> deleteImageIds
    ) {
        ProductPost post = findById(id);
        if (!post.isSeller(seller)) throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        String representativeUrl = post.getRepresentativeImageUrl();
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            representativeUrl = productImageService.deleteImages(post, deleteImageIds);
        }
        if (imageFiles != null && imageFiles.stream().anyMatch(file -> !file.isEmpty())) {
            representativeUrl = productImageService.appendImages(post, imageFiles);
        } else if ((deleteImageIds == null || deleteImageIds.isEmpty())
                && dto.getRepresentativeImageUrl() != null
                && !dto.getRepresentativeImageUrl().isBlank()) {
            representativeUrl = dto.getRepresentativeImageUrl();
        }
        post.update(dto.getTitle(), dto.getCategory(), dto.isGiveaway() ? 0 : dto.getPrice(), dto.isGiveaway(), dto.getContent(), representativeUrl, dto.getTradePlace());
    }

    @Transactional
    public void changeStatus(Long id, Member seller, ProductStatus status) {
        ProductPost post = findById(id);
        if (!post.isSeller(seller)) throw new IllegalArgumentException("작성자만 상태를 변경할 수 있습니다.");
        post.changeStatus(status);
    }

    /** Phase 3 Step 3(U9): 판매글 끌올 — cooldown 시간 경과 후 재끌올 (20260609) */
    @Transactional
    public void bump(Long id, Member seller) {
        ProductPost post = findById(id);
        if (!post.isSeller(seller)) {
            throw new IllegalArgumentException("작성자만 끌올할 수 있습니다.");
        }
        if (post.getStatus() == ProductStatus.HIDDEN) {
            throw new IllegalArgumentException("숨김 처리된 글은 끌올할 수 없습니다.");
        }
        if (!canBump(post)) {
            throw new IllegalArgumentException(
                    "끌올은 " + marketProperties.bumpCooldownHours() + "시간 후에 다시 할 수 있습니다.");
        }
        post.bump();
    }

    public boolean canBump(ProductPost post) {
        if (post.getBumpedAt() == null) {
            return true;
        }
        LocalDateTime nextAllowed = post.getBumpedAt().plusHours(marketProperties.bumpCooldownHours());
        return !LocalDateTime.now().isBefore(nextAllowed);
    }

    @Transactional
    public void hideByAdmin(Long id) {
        findById(id).hide();
    }
    public Page<ProductPost> findMyPosts(Member seller, int page) {
        return productPostRepository.findBySellerOrderByLatest(
                seller, PageRequest.of(Math.max(page, 0), 10));
    }
    private void requireLocalVerified(Member member) {
        if (member == null || !member.isLocalVerified() || member.getVerifiedNeighborhood() == null) throw new IllegalArgumentException("동네 인증 후 이용할 수 있습니다.");
    }
}
