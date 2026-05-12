package com.study.neighbortrade.controller;

import com.study.neighbortrade.domain.report.ReportTargetType;
import com.study.neighbortrade.dto.report.ReportRequestDto;
import com.study.neighbortrade.service.CurrentMemberService;
import com.study.neighbortrade.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;
    private final CurrentMemberService currentMemberService;

    @GetMapping("/{type}/{id}")
    public String form(@PathVariable String type, @PathVariable Long id, Model model) {
        model.addAttribute("targetType", ReportTargetType.valueOf(type.toUpperCase()));
        model.addAttribute("targetId", id);
        model.addAttribute("reportRequestDto", new ReportRequestDto());
        return "report/form";
    }

    @PostMapping("/{type}/{id}")
    public String create(@PathVariable String type, @PathVariable Long id, @Valid @ModelAttribute ReportRequestDto dto, BindingResult br, Model model, Principal principal) {
        ReportTargetType targetType = ReportTargetType.valueOf(type.toUpperCase());
        if (br.hasErrors()) {
            model.addAttribute("targetType", targetType);
            model.addAttribute("targetId", id);
            return "report/form";
        }
        reportService.create(currentMemberService.require(principal), targetType, id, dto);
        return "redirect:/reports/done";
    }

    @GetMapping("/done")
    public String done() {
        return "report/done";
    }
}
