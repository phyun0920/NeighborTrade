package com.study.neighbortrade.controller;

import com.study.neighbortrade.domain.community.CommunityPost;
import com.study.neighbortrade.domain.location.Neighborhood;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.dto.community.CommunityCommentRequestDto;
import com.study.neighbortrade.dto.community.CommunityPostRequestDto;
import com.study.neighbortrade.service.CommunityService;
import com.study.neighbortrade.service.CurrentMemberService;
import com.study.neighbortrade.web.BrowsingNeighborhoodResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * 동네생활 게시글·댓글 MVC.
 * <p>
 * Phase 3 Step 2(C1): list·detail GET 비로그인 공개 (20260609).<br>
 * Phase 3 Step 6(COM): list·detail v2 shell — search band·카드 UI, 제목 키워드 검색 (20260611).<br>
 * 근거: doc/20260529금_06리뉴얼_버전2_Phase3_01계획_01.md §Step 6, doc/20260611목_…_Step6_01.md
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/community")
public class CommunityController {
    private final CommunityService communityService;
    private final CurrentMemberService currentMemberService;
    private final BrowsingNeighborhoodResolver browsingNeighborhoodResolver;

    // Phase 3 Step 2(C1) + Step 6(COM): GET 공개 목록 — browsing·키워드·카드 v2 (20260609, 20260611)
    // keyword → community-search-band·community-pagination에서 page 이동 시 유지
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String keyword,
                       Model model, Principal principal, HttpServletRequest request) {
        Member member = currentMemberService.get(principal);
        Neighborhood browsing = browsingNeighborhoodResolver.resolve(request).orElse(null);
        Neighborhood listNeighborhood = communityService.resolveListNeighborhood(browsing, member);
        model.addAttribute("currentMember", member);
        model.addAttribute("listNeighborhood", listNeighborhood);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("page", communityService.list(browsing, member, keyword, page));
        return "community/list";
    }

    @GetMapping("/form")
    public String form(Model model, Principal principal) {
        model.addAttribute("currentMember", currentMemberService.require(principal));
        model.addAttribute("communityPostRequestDto", new CommunityPostRequestDto());
        return "community/form";
    }

    @PostMapping("/form")
    public String create(@Valid @ModelAttribute CommunityPostRequestDto dto, BindingResult br, Model model, Principal principal) {
        if (br.hasErrors()) {
            model.addAttribute("currentMember", currentMemberService.require(principal));
            return "community/form";
        }
        CommunityPost post = communityService.create(currentMemberService.require(principal), dto);
        return "redirect:/community/detail/" + post.getId();
    }

    // Phase 3 Step 2(C1) + Step 6(COM): GET 공개 상세 — v2 패널·search band (20260609, 20260611)
    // keyword="" — detail 상단 community-search-band fragment 기본값 (list와 동일 변수명)
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        CommunityPost post = communityService.detail(id);
        Member member = currentMemberService.get(principal);
        model.addAttribute("keyword", "");
        model.addAttribute("post", post);
        model.addAttribute("comments", communityService.comments(post));
        model.addAttribute("commentDto", new CommunityCommentRequestDto());
        model.addAttribute("currentMember", member);
        return "community/detail";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, Principal principal) {
        Member member = currentMemberService.require(principal);
        CommunityPost post = communityService.findById(id);
        if (!post.isAuthor(member)) throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        CommunityPostRequestDto dto = new CommunityPostRequestDto();
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        model.addAttribute("post", post);
        model.addAttribute("communityPostRequestDto", dto);
        return "community/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute CommunityPostRequestDto dto, BindingResult br, Model model, Principal principal) {
        if (br.hasErrors()) {
            model.addAttribute("post", communityService.findById(id));
            return "community/edit";
        }
        communityService.update(id, currentMemberService.require(principal), dto);
        return "redirect:/community/detail/" + id;
    }

    @PostMapping("/comment/{postId}")
    public String comment(@PathVariable Long postId, @Valid @ModelAttribute("commentDto") CommunityCommentRequestDto dto, BindingResult br, Principal principal) {
        if (!br.hasErrors()) {
            communityService.addComment(postId, currentMemberService.require(principal), dto);
        }
        return "redirect:/community/detail/" + postId;
    }

    @PostMapping("/comment/delete/{commentId}")
    public String deleteComment(@PathVariable Long commentId, @RequestParam Long postId, Principal principal) {
        communityService.deleteComment(commentId, currentMemberService.require(principal));
        return "redirect:/community/detail/" + postId;
    }
}
