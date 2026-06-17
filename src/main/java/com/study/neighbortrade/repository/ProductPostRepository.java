package com.study.neighbortrade.repository;

import com.study.neighbortrade.domain.location.Neighborhood;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.product.ProductCategory;
import com.study.neighbortrade.domain.product.ProductPost;
import com.study.neighbortrade.domain.product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductPostRepository extends JpaRepository<ProductPost, Long> {
    Page<ProductPost> findBySeller(Member seller, Pageable pageable);

    Page<ProductPost> findByStatus(ProductStatus status, Pageable pageable);

    @Query("""
            SELECT p
            FROM ProductPost p
            WHERE p.status <> 'HIDDEN'
              AND (
                :keyword IS NULL
                OR :keyword = ''
                OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<ProductPost> searchVisible(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM ProductPost p
            WHERE p.neighborhood = :neighborhood
              AND p.status <> 'HIDDEN'
              AND (
                :keyword IS NULL
                OR :keyword = ''
                OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<ProductPost> searchVisibleByNeighborhood(
            @Param("neighborhood") Neighborhood neighborhood,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM ProductPost p
            WHERE p.status <> 'HIDDEN'
              AND (:onlyOnSale = false OR p.status = 'ON_SALE')
              AND (:category IS NULL OR p.category = :category)
              AND (:neighborhoodId IS NULL OR p.neighborhood.id = :neighborhoodId)
              AND (
                :keyword IS NULL
                OR :keyword = ''
                OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<ProductPost> searchVisibleWithFilters(
            @Param("keyword") String keyword,
            @Param("category") ProductCategory category,
            @Param("neighborhoodId") Long neighborhoodId,
            @Param("onlyOnSale") boolean onlyOnSale,
            Pageable pageable
    );

    // Phase 3 Step 3(U9): 최신순 = COALESCE(bumpedAt, createdAt) (20260609)
    @Query("""
            SELECT p
            FROM ProductPost p
            WHERE p.status <> 'HIDDEN'
              AND (:onlyOnSale = false OR p.status = 'ON_SALE')
              AND (:category IS NULL OR p.category = :category)
              AND (:neighborhoodId IS NULL OR p.neighborhood.id = :neighborhoodId)
              AND (
                :keyword IS NULL
                OR :keyword = ''
                OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY COALESCE(p.bumpedAt, p.createdAt) DESC
            """)
    Page<ProductPost> searchVisibleWithFiltersLatest(
            @Param("keyword") String keyword,
            @Param("category") ProductCategory category,
            @Param("neighborhoodId") Long neighborhoodId,
            @Param("onlyOnSale") boolean onlyOnSale,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM ProductPost p
            WHERE p.seller = :seller
            ORDER BY COALESCE(p.bumpedAt, p.createdAt) DESC
            """)
    Page<ProductPost> findBySellerOrderByLatest(@Param("seller") Member seller, Pageable pageable);
}
