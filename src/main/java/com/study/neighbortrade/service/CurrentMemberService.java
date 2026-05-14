package com.study.neighbortrade.service;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.security.Principal;

@Service
@RequiredArgsConstructor
public class CurrentMemberService {
    private final MemberRepository memberRepository;
    public Member get(Principal principal) {
        if (principal == null) return null;
        String username = principal.getName();
        if (username == null
                || username.isBlank()
                || "anonymousUser".equals(username)) {
            return null;
        }
        return memberRepository.findByUsername(username).orElse(null);
    }
    public Member require(Principal principal) {
        Member member = get(principal);
        if (member == null) throw new IllegalArgumentException("로그인이 필요합니다.");
        return member;
    }
}
