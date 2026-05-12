package com.study.neighbortrade.repository;

import com.study.neighbortrade.domain.product.ProductImage;
import com.study.neighbortrade.domain.product.ProductPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductPostOrderBySortOrderAsc(ProductPost productPost);

    void deleteByProductPost(ProductPost productPost);
}
