package com.study.neighbortrade.controller;

import com.study.neighbortrade.domain.community.CommunityPost;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.dto.community.CommunityCommentRequestDto;
import com.study.neighbortrade.dto.community.CommunityPostRequestDto;
import com.study.neighbortrade.service.CommunityService;
import com.study.neighbortrade.service.CurrentMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/community")
public class CommunityController {
    private final CommunityService communityService;
    private final CurrentMemberService currentMemberService;

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page, Model model, Principal principal) {
        Member member = currentMemberService.require(principal);
        model.addAttribute("currentMember", member);
        model.addAttribute("page", communityService.list(member, page));
        return "community/list";
    }

    @GetMapping("/form")
    public String form(Model model, Principal principal) {
        model.addAttribute("currentMember", currentMemberService.require(principal));
        model.addAttribute("communityPostRequestDto", new CommunityPostRequestDto());
        return "community/form";
    }

    @PostMapping("/form")
    public String create(@Valid @ModelAttribute CommunityPostRequestDto dto, BindingResult br, Model model, Principal principal) {
        if (br.hasErrors()) {
            model.addAttribute("currentMember", currentMemberService.require(principal));
            return "community/form";
        }
        CommunityPost post = communityService.create(currentMemberService.require(principal), dto);
        return "redirect:/community/detail/" + post.getId();
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        CommunityPost post = communityService.detail(id);
        model.addAttribute("post", post);
        model.addAttribute("comments", communityService.comments(post));
        model.addAttribute("commentDto", new CommunityCommentRequestDto());
        model.addAttribute("currentMember", currentMemberService.require(principal));
        return "community/detail";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, Principal principal) {
        Member member = currentMemberService.require(principal);
        CommunityPost post = communityService.findById(id);
        if (!post.isAuthor(member)) throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        CommunityPostRequestDto dto = new CommunityPostRequestDto();
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        model.addAttribute("post", post);
        model.addAttribute("communityPostRequestDto", dto);
        return "community/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute CommunityPostRequestDto dto, BindingResult br, Model model, Principal principal) {
        if (br.hasErrors()) {
            model.addAttribute("post", communityService.findById(id));
            return "community/edit";
        }
        communityService.update(id, currentMemberService.require(principal), dto);
        return "redirect:/community/detail/" + id;
    }

    @PostMapping("/comment/{postId}")
    public String comment(@PathVariable Long postId, @Valid @ModelAttribute("commentDto") CommunityCommentRequestDto dto, BindingResult br, Principal principal) {
        if (!br.hasErrors()) {
            communityService.addComment(postId, currentMemberService.require(principal), dto);
        }
        return "redirect:/community/detail/" + postId;
    }

    @PostMapping("/comment/delete/{commentId}")
    public String deleteComment(@PathVariable Long commentId, @RequestParam Long postId, Principal principal) {
        communityService.deleteComment(commentId, currentMemberService.require(principal));
        return "redirect:/community/detail/" + postId;
    }
}
