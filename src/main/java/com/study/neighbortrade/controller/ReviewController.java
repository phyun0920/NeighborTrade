package com.study.neighbortrade.controller;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.dto.review.ReviewRequestDto;
import com.study.neighbortrade.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/review")
public class ReviewController {
    private final ReviewService reviewService;
    private final CurrentMemberService currentMemberService;

    @GetMapping("/form/{tradeId}")
    public String form(@PathVariable Long tradeId, Model model, Principal principal) {
        Member member = currentMemberService.require(principal);
        model.addAttribute("currentMember", member);
        model.addAttribute("trade", reviewService.findReviewableTrade(tradeId, member));
        model.addAttribute("reviewRequestDto", new ReviewRequestDto());
        return "review/form";
    }

    @PostMapping("/form/{tradeId}")
    public String create(@PathVariable Long tradeId,

    @Valid

    @ModelAttribute
    ReviewRequestDto dto, BindingResult br, Model model, Principal principal) {
        Member member = currentMemberService.require(principal);
        if (br.hasErrors()) {
            model.addAttribute("trade", reviewService.findReviewableTrade(tradeId, member));
            return "review/form";
        }
        reviewService.create(tradeId, member, dto);
        return "redirect:/review/my";
    }

    @GetMapping("/my")
    public String my(Model model, Principal principal) {
        Member member = currentMemberService.require(principal);
        model.addAttribute("currentMember", member);
        model.addAttribute("reviews", reviewService.findReceived(member));
        return "review/my";
    }
}
