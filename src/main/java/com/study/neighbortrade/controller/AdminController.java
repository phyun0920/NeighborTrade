package com.study.neighbortrade.controller;

import com.study.neighbortrade.admin.AdminMemberListRow;
import com.study.neighbortrade.domain.member.LoginType;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.member.MemberRole;
import com.study.neighbortrade.domain.report.ReportStatus;
import com.study.neighbortrade.repository.*;
import com.study.neighbortrade.repository.PopularSearchKeywordRepository;
import com.study.neighbortrade.service.CommunityService;
import com.study.neighbortrade.service.PopularSearchKeywordService;
import com.study.neighbortrade.service.WeeklyAggregateResult;
import com.study.neighbortrade.service.ProductPostService;
import com.study.neighbortrade.service.ReportService;
import com.study.neighbortrade.support.EmailMasking;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final PopularSearchKeywordService popularSearchKeywordService;
    private final PopularSearchKeywordRepository popularSearchKeywordRepository;
    private final SearchLogRepository searchLogRepository;

    @GetMapping( {
            "", "/dashboard"
    }
    ) public String dashboard(Model model) {
        model.addAttribute("memberCount", memberRepository.count());
        model.addAttribute("postCount", productPostRepository.count());
        model.addAttribute("tradeCount", tradeRepository.count());
        model.addAttribute("communityCount", communityPostRepository.count());
        model.addAttribute("reportCount", reportService.list(0).getTotalElements());
        var latestKeywords = popularSearchKeywordRepository.findLatestTop(PageRequest.of(0, 1));
        if (latestKeywords.isEmpty()) {
            model.addAttribute("popularKeywordCount", 0L);
            model.addAttribute("topPopularKeyword", null);
        } else {
            var period = latestKeywords.get(0).getPeriod();
            model.addAttribute("popularKeywordCount", popularSearchKeywordRepository.countByPeriod(period));
            model.addAttribute("topPopularKeyword", latestKeywords.get(0).getKeyword());
        }
        model.addAttribute("searchLogCount7d", searchLogCountLast7Days());
        model.addAttribute("adminActiveMenu", "dashboard");
        return "admin/dashboard";
    }

    private long searchLogCountLast7Days() {
        return searchLogRepository.countBySearchedAtAfter(LocalDateTime.now().minusDays(7));
    }

    @GetMapping("/members")
    public String members(Model model) {
        List<AdminMemberListRow> rows =
                memberRepository.findAllWithVerifiedNeighborhood().stream()
                        .map(this::toAdminMemberRow)
                        .toList();
        model.addAttribute("memberRows", rows);
        model.addAttribute("adminActiveMenu", "members");
        return "admin/members";
    }

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

    private static String roleLabelKo(MemberRole r) {
        if (r == null) return "-";
        return switch (r) {
            case ROLE_USER -> "일반";
            case ROLE_LOCAL_VERIFIED -> "인증회원";
            case ROLE_ADMIN -> "관리자";
        };
    }

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
        model.addAttribute("adminActiveMenu", "neighborhoods");
        return "admin/neighborhoods";
    }

    @GetMapping("/reports")
    public String reports(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("page", reportService.list(page));
        model.addAttribute("statuses", ReportStatus.values());
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

    // Phase 3 Step 4(B7-2): 인기검색어 관리 (20260609)
    @GetMapping("/popular-keywords")
    public String popularKeywords(Model model) {
        var keywords = popularSearchKeywordRepository.findLatestTop(PageRequest.of(0, 10));
        model.addAttribute("keywords", keywords);

        if (keywords.isEmpty()) {
            model.addAttribute("latestPeriod", null);
            model.addAttribute("periodLabel", "집계 없음");
            model.addAttribute("popularKeywordCount", 0L);
            model.addAttribute("usingFallback", true);
        } else {
            LocalDate period = keywords.get(0).getPeriod();
            model.addAttribute("latestPeriod", period);
            model.addAttribute("periodLabel", period + " ~ " + period.plusDays(6));
            model.addAttribute("popularKeywordCount", popularSearchKeywordRepository.countByPeriod(period));
            model.addAttribute("usingFallback", false);
        }
        model.addAttribute("searchLogCount7d", searchLogCountLast7Days());
        model.addAttribute("nextScheduledRun", "매주 월요일 00:00");
        model.addAttribute("adminActiveMenu", "popular-keywords");
        return "admin/popular-keywords";
    }

    @PostMapping("/popular-keywords/{id}")
    public String updatePopularKeyword(@PathVariable Long id, @RequestParam String keyword) {
        popularSearchKeywordService.updateKeyword(id, keyword);
        return "redirect:/admin/popular-keywords";
    }

    @PostMapping("/popular-keywords/{id}/delete")
    public String deletePopularKeyword(@PathVariable Long id) {
        popularSearchKeywordService.deleteKeyword(id);
        return "redirect:/admin/popular-keywords";
    }

    @PostMapping("/popular-keywords/aggregate")
    public String aggregatePopularKeywords(RedirectAttributes ra) {
        WeeklyAggregateResult result = popularSearchKeywordService.aggregateWeekly();
        ra.addFlashAttribute(
                "adminMessage",
                result.savedCount() + "건 집계 완료 (period: " + result.period() + ")");
        return "redirect:/admin/popular-keywords";
    }

    // admin UI Step B: search_v2_log read-only (20260616)
    @GetMapping("/search-logs")
    public String searchLogs(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute(
                "page",
                searchLogRepository.findAllByOrderBySearchedAtDesc(
                        PageRequest.of(Math.max(page, 0), 10, Sort.by("searchedAt").descending())));
        model.addAttribute("searchLogCount7d", searchLogCountLast7Days());
        model.addAttribute("adminActiveMenu", "search-logs");
        return "admin/search-logs";
    }
}
