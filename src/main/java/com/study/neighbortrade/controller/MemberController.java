package com.study.neighbortrade.controller;

import com.study.neighbortrade.dto.member.MemberJoinRequestDto;
import com.study.neighbortrade.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/login")
    public String login() {
        return "member/login";
    }

    @GetMapping("/join")
    public String join(Model model) {
        model.addAttribute("memberJoinRequestDto", new MemberJoinRequestDto());
        return "member/join";
    }

    @PostMapping("/join")
    public String join(@Valid

    @ModelAttribute
    MemberJoinRequestDto dto, BindingResult bindingResult, Model model, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) return "member/join";
        try {
            memberService.join(dto);
            ra.addAttribute("joined", "true");
            return "redirect:/member/login";
        }
        catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/join";
        }
    }
}
