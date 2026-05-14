package com.study.neighbortrade.controller;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.product.*;
import com.study.neighbortrade.dto.product.ProductPostRequestDto;
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

@Controller
@RequiredArgsConstructor
@RequestMapping("/market")
public class MarketController {
    private final ProductPostService productPostService;
    private final ProductImageService productImageService;
    private final CurrentMemberService currentMemberService;

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "") String keyword,

                       @RequestParam(defaultValue = "false")
                       boolean onlyOnSale,

                       @RequestParam(defaultValue = "0")
                       int page, Model model, Principal principal) {
        Member member = currentMemberService.get(principal);
        model.addAttribute("currentMember", member);
        model.addAttribute("page", productPostService.list(keyword, onlyOnSale, page));
        model.addAttribute("keyword", keyword);
        model.addAttribute("onlyOnSale", onlyOnSale);
        return "market/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        ProductPost post = productPostService.findDetail(id);
        model.addAttribute("post", post);
        model.addAttribute("images", productImageService.findByPost(post));
        model.addAttribute("currentMember", currentMemberService.get(principal));
        return "market/detail";
    }

    @GetMapping("/form")
    public String form(Model model, Principal principal) {
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
            model.addAttribute("categories", ProductCategory.values());
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
                       ProductPostRequestDto dto, BindingResult br, @RequestParam(name = "imageFiles", required = false) List<MultipartFile> imageFiles, Model model, Principal principal) {
        if (br.hasErrors()) {
            model.addAttribute("categories", ProductCategory.values());
            return "market/edit";
        }
        productPostService.update(id, currentMemberService.require(principal), dto, imageFiles);
        return "redirect:/market/detail/" + id;
    }

    @PostMapping("/status/{id}")
    public String status(@PathVariable Long id,

                         @RequestParam
                         ProductStatus status, Principal principal) {
        productPostService.changeStatus(id, currentMemberService.require(principal), status);
        return "redirect:/market/detail/" + id;
    }

    @GetMapping("/my")
    public String my(@RequestParam(defaultValue = "0") int page, Model model, Principal principal) {
        Member member = currentMemberService.require(principal);
        model.addAttribute("currentMember", member);
        model.addAttribute("page", productPostService.findMyPosts(member, page));
        return "market/my";
    }
}
