package com.study.neighbortrade.web;

import com.study.neighbortrade.domain.location.Neighborhood;
import com.study.neighbortrade.service.CurrentMemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
@RequiredArgsConstructor
public class BrowsingNeighborhoodModelAdvice {

    private final BrowsingNeighborhoodResolver browsingNeighborhoodResolver;
    private final CurrentMemberService currentMemberService;

    @ModelAttribute
    public void addBrowsingNeighborhood(HttpServletRequest request, Model model, Principal principal) {
        Neighborhood nh = browsingNeighborhoodResolver.resolve(request).orElse(null);
        if (nh == null && principal != null) {
            var member = currentMemberService.get(principal);
            if (member != null && member.isLocalVerified() && member.getVerifiedNeighborhood() != null) {
                nh = member.getVerifiedNeighborhood();
            }
        }
        if (nh != null) {
            model.addAttribute("browsingNeighborhood", nh);
            model.addAttribute("browsingNeighborhoodId", nh.getId());
        } else {
            model.addAttribute("browsingNeighborhood", null);
            model.addAttribute("browsingNeighborhoodId", null);
        }
        model.addAttribute("currentRedirect", buildSafeRedirectPath(request));
        model.addAttribute(
                "requestContextPath",
                request.getContextPath() != null ? request.getContextPath() : "");
    }

    /** 컨텍스트 경로 제외한 상대 경로 + 쿼리스트링 (redirect 파라미터용) */
    private static String buildSafeRedirectPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String cp = request.getContextPath();
        if (cp != null && !cp.isEmpty() && uri.startsWith(cp)) {
            uri = uri.substring(cp.length());
        }
        if (uri.isEmpty()) {
            uri = "/";
        }
        String qs = request.getQueryString();
        if (qs != null && !qs.isBlank()) {
            uri = uri + "?" + qs;
        }
        return uri;
    }
}
