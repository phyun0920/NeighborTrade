package com.study.neighbortrade.controller;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.dto.location.LocationVerifyRequestDto;
import com.study.neighbortrade.service.*;
import com.study.neighbortrade.web.BrowseNeighborhoodCookieSupport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/location")
public class LocationController {
    private final LocationService locationService;
    private final CurrentMemberService currentMemberService;

    @Value("${app.postgis.enabled:false}")
    private boolean postgisEnabled;    // PostGIS경계 안내문구 켜고 끄는 용도(20260512)

    @GetMapping("/select")
    public String select(Model model, Principal principal) {
        model.addAttribute("currentMember", currentMemberService.require(principal));
        model.addAttribute("neighborhoods", locationService.findAllNeighborhoodsForSelect());   // PostGIS경계 안내문구 켜고 끄는 용도(20260512)
        model.addAttribute("locationVerifyRequestDto", new LocationVerifyRequestDto());
        model.addAttribute("postgisEnabled", postgisEnabled);       // PostGIS경계 안내문구 켜고 끄는 용도(20260512)
        return "location/select";
    }

    @PostMapping("/verify")
    public String verify(
            @Valid @ModelAttribute LocationVerifyRequestDto dto,
            BindingResult bindingResult,
            Model model,
            Principal principal,
            HttpServletResponse response) {

        Member member = currentMemberService.require(principal);

        boolean inputOk = !bindingResult.hasErrors();
        boolean neighborhoodMatches = inputOk && locationService.verify(member, dto);

        if (neighborhoodMatches) {
            BrowseNeighborhoodCookieSupport.writeBrowsingNeighborhoodCookie(response, dto.getNeighborhoodId());
        }

        model.addAttribute("verified", neighborhoodMatches);
        model.addAttribute("currentMember", member);
        return "location/result";
    }

    @GetMapping("/my")
    public String my(Model model, Principal principal) {
        model.addAttribute("currentMember", currentMemberService.require(principal));
        return "location/my";
    }
}
