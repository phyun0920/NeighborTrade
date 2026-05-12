package com.study.neighbortrade.controller;

import com.study.neighbortrade.domain.product.ProductStatus;
import com.study.neighbortrade.domain.report.ReportStatus;
import com.study.neighbortrade.repository.*;
import com.study.neighbortrade.service.CommunityService;
import com.study.neighbortrade.service.ProductPostService;
import com.study.neighbortrade.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final MemberRepository memberRepository;
    private final ProductPostRepository productPostRepository;
    private final NeighborhoodRepository neighborhoodRepository;
    private final TradeRepository tradeRepository;
    private final ProductPostService productPostService;
    private final CommunityPostRepository communityPostRepository;
    private final ReportService reportService;
    private final CommunityService communityService;

    @GetMapping( {
        "", "/dashboard"
    }
    ) public String dashboard(Model model) {
        model.addAttribute("memberCount", memberRepository.count());
        model.addAttribute("postCount", productPostRepository.count());
        model.addAttribute("tradeCount", tradeRepository.count());
        model.addAttribute("communityCount", communityPostRepository.count());
        model.addAttribute("reportCount", reportService.list(0).getTotalElements());
        return "admin/dashboard";
    }

    @GetMapping("/members")
    public String members(Model model) {
        model.addAttribute("members", memberRepository.findAll());
        return "admin/members";
    }

    @GetMapping("/posts")
    public String posts(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("page", productPostRepository.findAll(PageRequest.of(Math.max(page, 0), 20, Sort.by("createdAt").descending())));
        return "admin/posts";
    }

    @PostMapping("/posts/hide/{id}")
    public String hide(@PathVariable Long id) {
        productPostService.hideByAdmin(id);
        return "redirect:/admin/posts";
    }

    @GetMapping("/neighborhoods")
    public String neighborhoods(Model model) {
        model.addAttribute("neighborhoods", neighborhoodRepository.findAll());
        return "admin/neighborhoods";
    }

    @GetMapping("/reports")
    public String reports(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("page", reportService.list(page));
        model.addAttribute("statuses", ReportStatus.values());
        return "admin/reports";
    }

    @PostMapping("/reports/process/{id}")
    public String processReport(@PathVariable Long id, @RequestParam ReportStatus status, @RequestParam(defaultValue = "") String processorNote) {
        reportService.process(id, status, processorNote);
        return "redirect:/admin/reports";
    }

    @PostMapping("/community/hide/{id}")
    public String hideCommunity(@PathVariable Long id) {
        communityService.hideByAdmin(id);
        return "redirect:/community/detail/" + id;
    }
}
