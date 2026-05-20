package com.study.neighbortrade.controller;

import com.study.neighbortrade.domain.chat.ChatMessage;
import com.study.neighbortrade.domain.chat.ChatRoom;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.dto.chat.ChatTimelineRow;
import com.study.neighbortrade.service.ChatService;
import com.study.neighbortrade.service.CurrentMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        // 20260518월 채팅방 타임라인 — 날짜 구분선 또는 메시지 한 줄 추가
        List<ChatMessage> messages = chatService.messages(room);
        Member counterpart =
                member.getId().equals(room.getSeller().getId()) ? room.getBuyer() : room.getSeller();
        model.addAttribute("currentMember", member);
        model.addAttribute("chatCounterpart", counterpart);
        model.addAttribute("room", room);
        // 20260518월 채팅방 타임라인 — 날짜 구분선 또는 메시지 한 줄 추가
        model.addAttribute("messages", messages);
        model.addAttribute("chatTimeline", buildTimeline(messages));
        return "chat/room";
    }

    // 20260518월 채팅방 타임라인 — 날짜 구분선 또는 메시지 한 줄
    private static List<ChatTimelineRow> buildTimeline(List<ChatMessage> messages) {
        if (messages.isEmpty()) {
            return List.of();
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREA);
        List<ChatTimelineRow> rows = new ArrayList<>();
        LocalDate lastDate = null;
        for (ChatMessage m : messages) {
            LocalDate d = m.getCreatedAt().toLocalDate();
            if (!d.equals(lastDate)) {
                rows.add(ChatTimelineRow.dateDivider(d.format(df)));
                lastDate = d;
            }
            rows.add(ChatTimelineRow.message(m));
        }
        return rows;
    }
}
