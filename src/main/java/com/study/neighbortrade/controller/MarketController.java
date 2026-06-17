package com.study.neighbortrade.controller;

import com.study.neighbortrade.config.MarketProperties;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.product.*;
import com.study.neighbortrade.dto.product.ProductPostRequestDto;
import com.study.neighbortrade.service.PopularSearchKeywordService;
import com.study.neighbortrade.service.SearchLogService;
import com.study.neighbortrade.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/market")
public class MarketController {
    private final ProductPostService productPostService;
    private final ProductImageService productImageService;
    private final CurrentMemberService currentMemberService;
    private final MarketProperties marketProperties;
    private final ProductFavoriteService productFavoriteService;
    private final LocationService locationService;
    private final SearchLogService searchLogService;
    private final PopularSearchKeywordService popularSearchKeywordService;

    @GetMapping("/list")
    public String list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "false") boolean onlyOnSale,
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) Long neighborhoodId,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "grid") String view,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model,
            Principal principal
    ) {
        MarketSort marketSort = MarketSort.fromParam(sort);
        MarketView marketView = MarketView.fromParam(view);
        Member member = currentMemberService.get(principal);
        var resultPage = productPostService.list(keyword, onlyOnSale, category, neighborhoodId, marketSort, page, size);

        // Phase 3 Step 4(B7-2): 검색 로깅
        if (!keyword.isBlank()) {
            var browsingNeighborhood = neighborhoodId != null ? locationService.findNeighborhoodById(neighborhoodId).orElse(null) : null;
            searchLogService.logSearch(keyword, browsingNeighborhood, member, (int) resultPage.getTotalElements());
        }

        model.addAttribute("currentMember", member);
        model.addAttribute("page", resultPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("onlyOnSale", onlyOnSale);
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("neighborhoodId", neighborhoodId);
        model.addAttribute("selectedNeighborhood", locationService.findNeighborhoodById(neighborhoodId).orElse(null));
        model.addAttribute("neighborhoodFilterGroups", locationService.findNeighborhoodFilterGroups());
        model.addAttribute("sort", marketSort.getParam());
        model.addAttribute("sortOptions", MarketSort.values());
        model.addAttribute("view", marketView.getParam());
        model.addAttribute("viewOptions", MarketView.values());
        model.addAttribute("pageSize", size);
        // Phase 3 Step 4(B7-2): 인기검색어 추가 (DB 비어있을 경우 YAML 폴백 보장)
        var popular = popularSearchKeywordService.getPopularKeywords();
        if (popular == null || popular.isEmpty()) {
            popular = marketProperties.popularKeywords();
        }
        model.addAttribute("popularKeywords", popular);
        model.addAttribute("paginationBase", "/market/list");
        List<Long> postIds = resultPage.getContent().stream().map(ProductPost::getId).toList();
        Set<Long> favoritedPostIds = productFavoriteService.findFavoritedPostIds(member, postIds);
        model.addAttribute("favoritedPostIds", favoritedPostIds);
        return "market/list";
    }

    private void addMarketShellAttributes(Model model) {
        model.addAttribute("keyword", "");
        model.addAttribute("onlyOnSale", false);
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("selectedCategory", null);
        model.addAttribute("neighborhoodId", null);
        model.addAttribute("sort", MarketSort.LATEST.getParam());
        model.addAttribute("view", MarketView.GRID.getParam());
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        ProductPost post = productPostService.findDetail(id);
        Member member = currentMemberService.get(principal);
        addMarketShellAttributes(model);
        model.addAttribute("post", post);
        model.addAttribute("images", productImageService.findByPost(post));
        model.addAttribute("currentMember", member);
        model.addAttribute("isFavorited", member != null
                && productFavoriteService.findFavoritedPostIds(member, List.of(id)).contains(id));
        model.addAttribute("canBump", member != null && post.isSeller(member) && productPostService.canBump(post));
        model.addAttribute("bumpCooldownHours", marketProperties.bumpCooldownHours());
        return "market/detail";
    }

    @GetMapping("/form")
    public String form(Model model, Principal principal) {
        addMarketShellAttributes(model);
        model.addAttribute("currentMember", currentMemberService.require(principal));
        model.addAttribute("productPostRequestDto", new ProductPostRequestDto());
        model.addAttribute("categories", ProductCategory.values());
        return "market/form";
    }

    @PostMapping("/form")
    public String create(@Valid

                         @ModelAttribute
                         ProductPostRequestDto dto, BindingResult br, @RequestParam(name = "imageFiles", required = false) List<MultipartFile> imageFiles, Model model, Principal principal) {
        if (br.hasErrors()) {
            addMarketShellAttributes(model);
            model.addAttribute("categories", ProductCategory.values());
            model.addAttribute("currentMember", currentMemberService.require(principal));
            return "market/form";
        }
        productPostService.create(currentMemberService.require(principal), dto, imageFiles);
        return "redirect:/market/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, Principal principal, RedirectAttributes ra) {
        ProductPost post = productPostService.findById(id);
        Member currentMember = currentMemberService.require(principal);
        if (!post.isSeller(currentMember)) {
            ra.addFlashAttribute("errorMessage", "작성자만 수정할 수 있습니다.");
            return "redirect:/market/detail/" + id;
        }
        addMarketShellAttributes(model);
        ProductPostRequestDto dto = new ProductPostRequestDto();
        dto.setTitle(post.getTitle());
        dto.setCategory(post.getCategory());
        dto.setPrice(post.getPrice());
        dto.setGiveaway(post.isGiveaway());
        dto.setContent(post.getContent());
        dto.setRepresentativeImageUrl(post.getRepresentativeImageUrl());
        dto.setTradePlace(post.getTradePlace());
        model.addAttribute("post", post);
        model.addAttribute("images", productImageService.findByPost(post));
        model.addAttribute("productPostRequestDto", dto);
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("currentMember", currentMember);
        return "market/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,

                       @Valid

                       @ModelAttribute
                       ProductPostRequestDto dto, BindingResult br, @RequestParam(name = "imageFiles", required = false) List<MultipartFile> imageFiles, @RequestParam(name = "deleteImageIds", required = false) List<Long> deleteImageIds, Model model, Principal principal) {
        if (br.hasErrors()) {
            addMarketShellAttributes(model);
            model.addAttribute("post", productPostService.findById(id));
            model.addAttribute("images", productImageService.findByPost(productPostService.findById(id)));
            model.addAttribute("categories", ProductCategory.values());
            model.addAttribute("currentMember", currentMemberService.require(principal));
            return "market/edit";
        }
        productPostService.update(id, currentMemberService.require(principal), dto, imageFiles, deleteImageIds);
        return "redirect:/market/detail/" + id;
    }

    @PostMapping("/status/{id}")
    public String status(@PathVariable Long id,

                         @RequestParam
                         ProductStatus status, Principal principal) {
        productPostService.changeStatus(id, currentMemberService.require(principal), status);
        return "redirect:/market/detail/" + id;
    }

    // Phase 3 Step 3(U9): 판매글 끌올 (20260609)
    @PostMapping("/bump/{id}")
    public String bump(
            @PathVariable Long id,
            @RequestParam(required = false) String redirect,
            Principal principal,
            RedirectAttributes ra
    ) {
        try {
            productPostService.bump(id, currentMemberService.require(principal));
            ra.addFlashAttribute("successMessage", "끌올되었습니다. 목록 상단에 노출됩니다.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        if (redirect != null && !redirect.isBlank()) {
            return "redirect:" + redirect;
        }
        return "redirect:/market/detail/" + id;
    }

    @GetMapping("/my")
    public String my(@RequestParam(defaultValue = "0") int page, Model model, Principal principal) {
        Member member = currentMemberService.require(principal);
        model.addAttribute("currentMember", member);
        model.addAttribute("page", productPostService.findMyPosts(member, page));
        model.addAttribute("bumpCooldownHours", marketProperties.bumpCooldownHours());
        return "market/my";
    }
}
