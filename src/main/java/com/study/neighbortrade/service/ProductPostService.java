package com.study.neighbortrade.service;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.product.ProductPost;
import com.study.neighbortrade.domain.product.ProductStatus;
import com.study.neighbortrade.dto.product.ProductPostRequestDto;
import com.study.neighbortrade.repository.ProductPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductPostService {
    private final ProductPostRepository productPostRepository;
    private final ProductImageService productImageService;
    public Page<ProductPost> list(String keyword, boolean onlyOnSale, int page) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), 12, Sort.by("createdAt").descending());
        if (onlyOnSale) return productPostRepository.findByStatus(ProductStatus.ON_SALE, pageable);
        return productPostRepository.searchVisible(keyword, pageable);
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
    public void update(Long id, Member seller, ProductPostRequestDto dto, List<MultipartFile> imageFiles) {
        ProductPost post = findById(id);
        if (!post.isSeller(seller)) throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        String representativeUrl = productImageService.replaceImages(post, imageFiles);
        if (imageFiles == null || imageFiles.stream().allMatch(MultipartFile::isEmpty)) {
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

    @Transactional
    public void hideByAdmin(Long id) {
        findById(id).hide();
    }
    public Page<ProductPost> findMyPosts(Member seller, int page) {
        return productPostRepository.findBySeller(seller, PageRequest.of(Math.max(page, 0), 10, Sort.by("createdAt").descending()));
    }
    private void requireLocalVerified(Member member) {
        if (member == null || !member.isLocalVerified() || member.getVerifiedNeighborhood() == null) throw new IllegalArgumentException("동네 인증 후 이용할 수 있습니다.");
    }
}
