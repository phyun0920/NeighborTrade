package com.study.neighbortrade.service;

import com.study.neighbortrade.domain.product.ProductImage;
import com.study.neighbortrade.domain.product.ProductPost;
import com.study.neighbortrade.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImageService {
    private static final int MAX_IMAGES_PER_POST = 10;

    private final ProductImageRepository productImageRepository;
    private final FileStorageService fileStorageService;

    public List<ProductImage> findByPost(ProductPost post) {
        return productImageRepository.findByProductPostOrderBySortOrderAsc(post);
    }

    @Transactional
    public String replaceImages(ProductPost post, List<MultipartFile> files) {
        if (files == null || files.stream().allMatch(MultipartFile::isEmpty)) {
            return post.getRepresentativeImageUrl();
        }
        productImageRepository.deleteByProductPost(post);
        return saveNewImages(post, files, 0, null);
    }

    /** 수정 시 기존 이미지는 유지하고 새 파일만 추가 */
    @Transactional
    public String appendImages(ProductPost post, List<MultipartFile> files) {
        if (files == null || files.stream().allMatch(MultipartFile::isEmpty)) {
            return post.getRepresentativeImageUrl();
        }
        List<ProductImage> existing = productImageRepository.findByProductPostOrderBySortOrderAsc(post);
        int startOrder = existing.size();
        if (startOrder >= MAX_IMAGES_PER_POST) {
            throw new IllegalArgumentException("상품 이미지는 최대 " + MAX_IMAGES_PER_POST + "장까지 등록할 수 있습니다.");
        }
        return saveNewImages(post, files, startOrder, post.getRepresentativeImageUrl());
    }

    /** 수정 시 선택한 기존 이미지 삭제. 대표 이미지가 삭제되면 남은 첫 장을 대표로 반환 */
    @Transactional
    public String deleteImages(ProductPost post, List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return post.getRepresentativeImageUrl();
        }
        Set<Long> targets = new HashSet<>(imageIds);
        List<ProductImage> all = productImageRepository.findByProductPostOrderBySortOrderAsc(post);
        String currentRepUrl = post.getRepresentativeImageUrl();
        boolean deletedRepresentative = false;

        for (ProductImage image : all) {
            if (!targets.contains(image.getId())) {
                continue;
            }
            if (image.isRepresentative()
                    || (currentRepUrl != null && currentRepUrl.equals(image.getImageUrl()))) {
                deletedRepresentative = true;
            }
            productImageRepository.delete(image);
        }

        List<ProductImage> remaining = productImageRepository.findByProductPostOrderBySortOrderAsc(post);
        if (remaining.isEmpty()) {
            return null;
        }
        if (deletedRepresentative) {
            return remaining.get(0).getImageUrl();
        }
        return currentRepUrl;
    }

    private String saveNewImages(
            ProductPost post,
            List<MultipartFile> files,
            int startOrder,
            String currentRepresentativeUrl
    ) {
        String representativeUrl = currentRepresentativeUrl;
        int order = startOrder;
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            if (order >= MAX_IMAGES_PER_POST) {
                throw new IllegalArgumentException("상품 이미지는 최대 " + MAX_IMAGES_PER_POST + "장까지 등록할 수 있습니다.");
            }
            FileStorageService.StoredFile stored = fileStorageService.storeProductImage(file);
            boolean representative = order == 0;
            if (representative) {
                representativeUrl = stored.getUrl();
            }
            productImageRepository.save(ProductImage.builder()
                    .productPost(post)
                    .originalFilename(stored.getOriginalFilename())
                    .storedFilename(stored.getStoredFilename())
                    .imageUrl(stored.getUrl())
                    .contentType(stored.getContentType())
                    .fileSize(stored.getSize())
                    .sortOrder(order++)
                    .representative(representative)
                    .build());
        }
        if (representativeUrl == null || representativeUrl.isBlank()) {
            return post.getRepresentativeImageUrl();
        }
        return representativeUrl;
    }
}
