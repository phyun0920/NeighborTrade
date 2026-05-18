package com.study.neighbortrade.controller;

import com.study.neighbortrade.admin.AdminMemberListRow;
import com.study.neighbortrade.domain.member.LoginType;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.member.MemberRole;
import com.study.neighbortrade.domain.report.ReportStatus;
import com.study.neighbortrade.repository.*;
import com.study.neighbortrade.service.CommunityService;
import com.study.neighbortrade.service.ProductPostService;
import com.study.neighbortrade.service.ReportService;
import com.study.neighbortrade.support.EmailMasking;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        // 20260515금 관리자메뉴 구현에서 dashboard 추가
        model.addAttribute("adminActiveMenu", "dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/members")
    public String members(Model model) {
        // 20260515금 관리자메뉴 구현에서 회원관리 표에 넣을 컬럼 리스트
        // model.addAttribute("members", memberRepository.findAll());
        List<AdminMemberListRow> rows =
                memberRepository.findAllWithVerifiedNeighborhood().stream()
                        .map(this::toAdminMemberRow)
                        .toList();
        model.addAttribute("memberRows", rows);
        model.addAttribute("adminActiveMenu", "members");
        return "admin/members";
    }

    // 20260515금 관리자메뉴 구현에서 회원관리 표에 넣을 컬럼 리스트
    private AdminMemberListRow toAdminMemberRow(Member m) {
        String nh =
                m.getVerifiedNeighborhood() != null
                        ? m.getVerifiedNeighborhood().getDisplayName()
                        : "-";
        String provider =
                m.getProvider() != null && !m.getProvider().isBlank()
                        ? m.getProvider()
                        : "-";
        return new AdminMemberListRow(
                m.getId(),
                m.getUsername(),
                m.getNickname(),
                EmailMasking.mask(m.getEmail()),
                roleLabelKo(m.getRole()),
                loginLabelKo(m.getLoginType()),
                provider,
                m.isLocalVerified(),
                nh,
                m.getMannerScore(),
                m.getCreatedAt());
    }

    // 20260515금 관리자메뉴 구현에서 회원관리 표에 넣을 컬럼 리스트 - 권한
    private static String roleLabelKo(MemberRole r) {
        if (r == null) return "-";
        return switch (r) {
            case ROLE_USER -> "일반";
            case ROLE_LOCAL_VERIFIED -> "인증회원";
            case ROLE_ADMIN -> "관리자";
        };
    }

    // 20260515금 관리자메뉴 구현에서 회원관리 표에 넣을 컬럼 리스트 - 로그인 종류
    private static String loginLabelKo(LoginType t) {
        if (t == null) return "-";
        return switch (t) {
            case LOCAL -> "로컬";
            case KAKAO -> "카카오";
            case NAVER -> "네이버";
        };
    }

    @GetMapping("/posts")
    public String posts(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("page", productPostRepository.findAll(PageRequest.of(Math.max(page, 0), 20, Sort.by("createdAt").descending())));
        // 20260515금 관리자메뉴 구현에서 posts(판매글)
        model.addAttribute("adminActiveMenu", "posts");
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
        // 20260515금 관리자메뉴 구현에서 neighborhoods(동네관리)
        model.addAttribute("adminActiveMenu", "neighborhoods");
        return "admin/neighborhoods";
    }

    @GetMapping("/reports")
    public String reports(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("page", reportService.list(page));
        model.addAttribute("statuses", ReportStatus.values());
        // 20260515금 관리자메뉴 구현에서 reports(신고관리)
        model.addAttribute("adminActiveMenu", "reports");
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
