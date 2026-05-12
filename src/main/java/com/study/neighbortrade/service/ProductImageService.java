package com.study.neighbortrade.service;

import com.study.neighbortrade.domain.product.ProductImage;
import com.study.neighbortrade.domain.product.ProductPost;
import com.study.neighbortrade.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImageService {
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
        String representativeUrl = null;
        int order = 0;
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
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
        return representativeUrl == null ? post.getRepresentativeImageUrl() : representativeUrl;
    }
}
