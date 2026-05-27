package com.study.neighbortrade.controller;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.product.ProductPost;
import com.study.neighbortrade.service.CurrentMemberService;
import com.study.neighbortrade.service.ProductFavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/market")
public class MarketFavoriteController {
    private final ProductFavoriteService productFavoriteService;
    private final CurrentMemberService currentMemberService;

    @PostMapping("/favorite/{postId}")
    public String toggle(
            @PathVariable Long postId,
            @RequestParam(required = false) String redirect,
            HttpServletRequest request,
            Principal principal
    ) {
        Member member = currentMemberService.require(principal);
        productFavoriteService.toggle(member, postId);
        return "redirect:" + resolveRedirect(redirect, request);
    }

    @GetMapping("/favorites")
    public String favorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model,
            Principal principal
    ) {
        Member member = currentMemberService.require(principal);
        Page<ProductPost> favoritePage = productFavoriteService.findMyFavorites(member, page, size);
        Set<Long> favoritedPostIds = favoritePage.getContent().stream()
                .map(ProductPost::getId)
                .collect(Collectors.toSet());

        model.addAttribute("currentMember", member);
        model.addAttribute("page", favoritePage);
        model.addAttribute("pageSize", size);
        model.addAttribute("favoritedPostIds", favoritedPostIds);
        model.addAttribute("paginationBase", "/market/favorites");
        model.addAttribute("allFavorites", true);
        return "market/favorites";
    }

    private static String resolveRedirect(String redirect, HttpServletRequest request) {
        String target = sanitizeRedirect(redirect);
        if (target != null) {
            return target;
        }
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            try {
                URI uri = URI.create(referer.trim());
                String path = uri.getRawPath();
                if (path != null && !path.isBlank()) {
                    String contextPath = request.getContextPath();
                    if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
                        path = path.substring(contextPath.length());
                    }
                    if (path.isEmpty()) {
                        path = "/";
                    }
                    target = sanitizeRedirect(path);
                    if (target != null) {
                        String query = uri.getRawQuery();
                        if (query != null && !query.isBlank()) {
                            return target + "?" + query;
                        }
                        return target;
                    }
                }
            } catch (IllegalArgumentException ignored) {
                // fall through
            }
        }
        return "/market/list";
    }

    private static String sanitizeRedirect(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return null;
        }
        redirect = redirect.trim();
        int queryIndex = redirect.indexOf('?');
        String path = queryIndex >= 0 ? redirect.substring(0, queryIndex) : redirect;
        if (!path.startsWith("/") || path.startsWith("//")) {
            return null;
        }
        return redirect;
    }
}
