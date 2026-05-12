package com.study.neighbortrade.service;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.member.MemberRole;
import com.study.neighbortrade.dto.member.MemberJoinRequestDto;
import com.study.neighbortrade.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void join(MemberJoinRequestDto dto) {
        if (memberRepository.existsByUsername(dto.getUsername())) throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        if (memberRepository.existsByEmail(dto.getEmail())) throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        memberRepository.save(Member.builder().username(dto.getUsername()).password(passwordEncoder.encode(dto.getPassword())).email(dto.getEmail()).nickname(dto.getNickname()).role(MemberRole.ROLE_USER).mannerScore(36.5).build());
    }
}
