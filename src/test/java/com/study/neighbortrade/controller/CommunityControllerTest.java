package com.study.neighbortrade.controller;

import com.study.neighbortrade.domain.community.CommunityPost;
import com.study.neighbortrade.domain.community.CommunityPostStatus;
import com.study.neighbortrade.domain.location.Neighborhood;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.service.CommunityService;
import com.study.neighbortrade.service.CurrentMemberService;
import com.study.neighbortrade.web.BrowsingNeighborhoodResolver;
import com.study.neighbortrade.web.RelativeTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Phase 3 Step 2(C1)·Step 6(COM) — {@code CommunityController} MVC 슬라이스 테스트.
 * <p>
 * 작성일: 2026-06-09 (C1), 2026-06-11 (COM v2 템플릿)<br>
 * 목적: 비로그인 {@code GET /community/list}·{@code /community/detail/{id}} 200·뷰 이름 검증.<br>
 * Step 6: detail v2가 {@code @relativeTime.communityMeta} 사용 → {@link RelativeTimeFormatter} Import.
 */
@WebMvcTest(controllers = CommunityController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RelativeTimeFormatter.class) // Phase 3 Step 6(COM) — detail v2 템플릿 렌더 (20260611)
class CommunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommunityService communityService;
    @MockBean
    private CurrentMemberService currentMemberService;
    @MockBean
    private BrowsingNeighborhoodResolver browsingNeighborhoodResolver;

    @BeforeEach
    void setUp() {
        when(currentMemberService.get(any())).thenReturn(null);
        when(browsingNeighborhoodResolver.resolve(any())).thenReturn(Optional.empty());
        when(communityService.resolveListNeighborhood(isNull(), isNull())).thenReturn(null);
        when(communityService.list(isNull(), isNull(), isNull(), eq(0))) // Step 6(COM): keyword 인자 추가 (20260611)
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 15), 0));
    }

    private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder builder) {
        CsrfToken token = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-csrf-token");
        return builder.requestAttr(CsrfToken.class.getName(), token).requestAttr("_csrf", token);
    }

    @Test
    void communityListReturnsOkForAnonymous() throws Exception {
        mockMvc.perform(withCsrf(get("/community/list")))
                .andExpect(status().isOk())
                .andExpect(view().name("community/list"));
    }

    @Test
    void communityDetailReturnsOkForAnonymous() throws Exception {
        Member author = Member.builder().id(1L).nickname("가산이웃").build();
        Neighborhood neighborhood = Neighborhood.builder().emdName("가산동").build();
        CommunityPost post = CommunityPost.builder()
                .id(1L)
                .author(author)
                .neighborhood(neighborhood)
                .title("테스트 글")
                .content("내용")
                .status(CommunityPostStatus.VISIBLE)
                .viewCount(0)
                .createdAt(LocalDateTime.now()) // Step 6(COM): @relativeTime.communityMeta (20260611)
                .build();
        when(communityService.detail(1L)).thenReturn(post);
        when(communityService.comments(post)).thenReturn(List.of());

        mockMvc.perform(withCsrf(get("/community/detail/1")))
                .andExpect(status().isOk())
                .andExpect(view().name("community/detail"));
    }
}
