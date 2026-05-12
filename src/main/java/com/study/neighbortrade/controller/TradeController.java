package com.study.neighbortrade.controller;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/trade")
public class TradeController {
    private final TradeService tradeService;
    private final CurrentMemberService currentMemberService;

    @PostMapping("/request/{postId}")
    public String request(@PathVariable Long postId, Principal principal, RedirectAttributes ra) {
        try {
            tradeService.request(postId, currentMemberService.require(principal));
        }
        catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/market/detail/" + postId;
        }
        return "redirect:/trade/list";
    }

    @GetMapping("/list")
    public String list(Model model, Principal principal) {
        Member member = currentMemberService.require(principal);
        model.addAttribute("currentMember", member);
        model.addAttribute("trades", tradeService.findMyTrades(member));
        return "trade/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        Member member = currentMemberService.require(principal);
        model.addAttribute("currentMember", member);
        model.addAttribute("trade", tradeService.findForMember(id, member));
        return "trade/detail";
    }

    @PostMapping("/accept/{id}")
    public String accept(@PathVariable Long id, Principal principal) {
        tradeService.accept(id, currentMemberService.require(principal));
        return "redirect:/trade/detail/" + id;
    }

    @PostMapping("/complete/{id}")
    public String complete(@PathVariable Long id, Principal principal) {
        tradeService.complete(id, currentMemberService.require(principal));
        return "redirect:/trade/detail/" + id;
    }

    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable Long id, Principal principal) {
        tradeService.cancel(id, currentMemberService.require(principal));
        return "redirect:/trade/list";
    }
}
