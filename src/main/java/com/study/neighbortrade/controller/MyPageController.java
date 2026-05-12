package com.study.neighbortrade.controller;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MyPageController {
    private final CurrentMemberService currentMemberService;
    private final ProductPostService productPostService;
    private final TradeService tradeService;
    private final ReviewService reviewService;

    @GetMapping("/mypage")
    public String mypage(Model model, Principal principal) {
        Member member = currentMemberService.require(principal);
        model.addAttribute("currentMember", member);
        model.addAttribute("myPosts", productPostService.findMyPosts(member, 0));
        model.addAttribute("trades", tradeService.findMyTrades(member));
        model.addAttribute("reviews", reviewService.findReceived(member));
        return "mypage";
    }
}
