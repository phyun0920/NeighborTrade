package com.study.neighbortrade.controller;

import com.study.neighbortrade.repository.NeighborhoodRepository;
import com.study.neighbortrade.web.BrowseNeighborhoodCookieSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class BrowseRegionController {

    private final NeighborhoodRepository neighborhoodRepository;

    @PostMapping("/browse/neighborhood")
    public String setBrowsingNeighborhood(
            @RequestParam Long neighborhoodId,
            @RequestParam(defaultValue = "/") String redirect,
            jakarta.servlet.http.HttpServletResponse response) {

        redirect = sanitizeRedirect(redirect);
        if (neighborhoodRepository.findById(neighborhoodId).isEmpty()) {
            return "redirect:" + redirect;
        }

        BrowseNeighborhoodCookieSupport.writeBrowsingNeighborhoodCookie(response, neighborhoodId);

        return "redirect:" + redirect;
    }

    @PostMapping("/browse/neighborhood/clear")
    public String clearBrowsingNeighborhood(
            @RequestParam(defaultValue = "/") String redirect, jakarta.servlet.http.HttpServletResponse response) {

        redirect = sanitizeRedirect(redirect);

        BrowseNeighborhoodCookieSupport.clearBrowsingNeighborhoodCookie(response);

        return "redirect:" + redirect;
    }

    /** 오픈 리다이렉트 방지: 같은 애플리케이션 내 상대 경로만 허용 */
    private static String sanitizeRedirect(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return "/";
        }
        redirect = redirect.trim();
        if (!redirect.startsWith("/") || redirect.startsWith("//")) {
            return "/";
        }
        return redirect;
    }
}
