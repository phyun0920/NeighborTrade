package com.study.neighbortrade.service;

import com.study.neighbortrade.config.MarketProperties;
import com.study.neighbortrade.domain.location.Neighborhood;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.product.ProductCategory;
import com.study.neighbortrade.domain.product.ProductPost;
import com.study.neighbortrade.domain.product.ProductStatus;
import com.study.neighbortrade.repository.ProductPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Phase 3 Step 3(U9) — 끌올 서비스 단위 테스트 (20260609).
 */
@ExtendWith(MockitoExtension.class)
class ProductPostServiceTest {

    @Mock
    private ProductPostRepository productPostRepository;
    @Mock
    private ProductImageService productImageService;
    @Mock
    private MarketProperties marketProperties;

    @InjectMocks
    private ProductPostService productPostService;

    private Member seller;
    private ProductPost post;

    @BeforeEach
    void setUp() {
        lenient().when(marketProperties.bumpCooldownHours()).thenReturn(24);
        seller = Member.builder().id(1L).nickname("판매자").localVerified(true).build();
        Neighborhood neighborhood = Neighborhood.builder().id(10L).emdName("가산동").build();
        post = ProductPost.builder()
                .id(100L)
                .seller(seller)
                .neighborhood(neighborhood)
                .title("테스트")
                .content("내용")
                .category(ProductCategory.DIGITAL)
                .price(1000)
                .status(ProductStatus.ON_SALE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void bumpSetsBumpedAtForSeller() {
        when(productPostRepository.findById(100L)).thenReturn(Optional.of(post));
        productPostService.bump(100L, seller);
        assertThat(post.getBumpedAt()).isNotNull();
    }

    @Test
    void bumpRejectsNonSeller() {
        when(productPostRepository.findById(100L)).thenReturn(Optional.of(post));
        Member other = Member.builder().id(2L).build();
        assertThatThrownBy(() -> productPostService.bump(100L, other))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("작성자만");
    }

    @Test
    void canBumpFalseWithinCooldown() {
        post.bump();
        assertThat(productPostService.canBump(post)).isFalse();
    }

    @Test
    void canBumpTrueWhenNeverBumped() {
        assertThat(productPostService.canBump(post)).isTrue();
    }
}
