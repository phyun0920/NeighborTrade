package com.study.neighbortrade.controller;

import com.study.neighbortrade.config.MarketProperties;
import com.study.neighbortrade.domain.product.MarketSort;
import com.study.neighbortrade.domain.product.ProductCategory;
import com.study.neighbortrade.service.*;
import com.study.neighbortrade.web.BrowsingNeighborhoodResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Phase 2 Step 8(8-2) — {@code HomeController}·{@code MarketController} MVC 슬라이스 테스트.
 * <p>
 * 작성일: 2026-05-29<br>
 * 목적: {@code GET /} redirect, {@code GET /market/list} 200·뷰 이름 등 최소 회귀 검증(CI에서 {@code ./gradlew test} 실행용).<br>
 * 근거: doc/20260528목_05리뉴얼_버전2_분석02_Phase2_03설명_08Step8.md §4.2, P2-T8
 */
@WebMvcTest(controllers = {HomeController.class, MarketController.class})
@AutoConfigureMockMvc(addFilters = false)
class MarketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductPostService productPostService;
    @MockBean
    private ProductImageService productImageService;
    @MockBean
    private CurrentMemberService currentMemberService;
    @MockBean
    private MarketProperties marketProperties;
    @MockBean
    private ProductFavoriteService productFavoriteService;
    @MockBean
    private LocationService locationService;
    @MockBean
    private BrowsingNeighborhoodResolver browsingNeighborhoodResolver;
    @MockBean
    private SearchLogService searchLogService;
    @MockBean
    private PopularSearchKeywordService popularSearchKeywordService;

    @BeforeEach
    void setUp() {
        when(currentMemberService.get(any())).thenReturn(null);
        when(browsingNeighborhoodResolver.resolve(any())).thenReturn(Optional.empty());
        when(marketProperties.popularKeywords()).thenReturn(List.of("에어컨", "자전거"));
        when(marketProperties.bumpCooldownHours()).thenReturn(24);
        when(locationService.findNeighborhoodById(any())).thenReturn(Optional.empty());
        when(locationService.findNeighborhoodFilterGroups()).thenReturn(List.of());
        when(productFavoriteService.findFavoritedPostIds(isNull(), anyList())).thenReturn(Set.of());
        when(productPostService.list(anyString(), anyBoolean(), any(), any(), any(MarketSort.class), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 8), 0));
        when(popularSearchKeywordService.getPopularKeywords()).thenReturn(List.of("에어컨", "자전거"));
    }

    // Thymeleaf header fragment가 _csrf 를 참조 — Security 필터 비활성 시 요청 attr 로 주입 (20260529 Step 8)
    private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder builder) {
        CsrfToken token = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-csrf-token");
        return builder.requestAttr(CsrfToken.class.getName(), token).requestAttr("_csrf", token);
    }

    @Test
        // T8-1: 안 B — / 는 /market/list 로 redirect (20260529 Step 8)
    void homeRedirectsToMarketList() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/market/list"));
    }

    @Test
        // T8-2: 비로그인 list 200, view market/list (20260529 Step 8)
    void marketListReturnsOk() throws Exception {
        mockMvc.perform(withCsrf(get("/market/list")))
                .andExpect(status().isOk())
                .andExpect(view().name("market/list"));
    }

    @Test
        // T8-3: Phase 2 sort·view·category 쿼리 유지 (20260529 Step 8)
    void marketListAcceptsPhase2QueryParams() throws Exception {
        mockMvc.perform(withCsrf(get("/market/list")
                        .param("sort", "price_asc")
                        .param("view", "list")
                        .param("category", ProductCategory.DIGITAL.name())))
                .andExpect(status().isOk())
                .andExpect(view().name("market/list"));
    }
}
