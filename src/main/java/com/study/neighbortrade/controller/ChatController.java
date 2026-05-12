package com.study.neighbortrade.controller;

import com.study.neighbortrade.domain.chat.ChatRoom;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.service.ChatService;
import com.study.neighbortrade.service.CurrentMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;
    private final CurrentMemberService currentMemberService;

    @PostMapping("/start/{productPostId}")
    public String start(@PathVariable Long productPostId, Principal principal) {
        ChatRoom room = chatService.findOrCreateRoom(productPostId, currentMemberService.require(principal));
        return "redirect:/chat/room/" + room.getId();
    }

    @GetMapping("/rooms")
    public String rooms(Model model, Principal principal) {
        Member member = currentMemberService.require(principal);
        model.addAttribute("currentMember", member);
        model.addAttribute("rooms", chatService.myRooms(member));
        return "chat/rooms";
    }

    @GetMapping("/room/{roomId}")
    public String room(@PathVariable Long roomId, Model model, Principal principal) {
        Member member = currentMemberService.require(principal);
        ChatRoom room = chatService.findRoom(roomId, member);
        model.addAttribute("currentMember", member);
        model.addAttribute("room", room);
        model.addAttribute("messages", chatService.messages(room));
        return "chat/room";
    }
}
