package com.study.neighbortrade.controller;

import com.study.neighbortrade.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final ProductPostService productPostService;
    private final CurrentMemberService currentMemberService;

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        model.addAttribute("currentMember", currentMemberService.get(principal));
        model.addAttribute("page", productPostService.list("", false, 0));
        return "index";
    }
}
