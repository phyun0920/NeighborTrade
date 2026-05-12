package com.study.neighbortrade.controller;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.dto.location.LocationVerifyRequestDto;
import com.study.neighbortrade.service.CurrentMemberService;
import com.study.neighbortrade.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        model.addAttribute("neighborhoods", locationService.findAllNeighborhoodsForSelect());
        model.addAttribute("locationVerifyRequestDto", new LocationVerifyRequestDto());    // PostGIS경계 안내문구 켜고 끄는 용도(20260512)
        model.addAttribute("postgisEnabled", postgisEnabled);
        return "location/select";
    }

    @PostMapping("/verify")
    public String verify(@Valid

    @ModelAttribute
    LocationVerifyRequestDto dto, BindingResult br, Model model, Principal principal) {
        Member member = currentMemberService.require(principal);
        boolean ok = !br.hasErrors() && locationService.verify(member, dto);
        model.addAttribute("verified", ok);
        model.addAttribute("currentMember", member);
        return "location/result";
    }

    @GetMapping("/my")
    public String my(Model model, Principal principal) {
        model.addAttribute("currentMember", currentMemberService.require(principal));
        return "location/my";
    }
}
