package com.study.neighbortrade.service;

import com.study.neighbortrade.domain.member.LoginType;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.member.MemberRole;
import com.study.neighbortrade.repository.MemberRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = delegate.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        SocialProfile profile = extractProfile(provider, oauthUser.getAttributes());
        Member member = memberRepository.findByProviderAndProviderId(provider, profile.providerId())
                .map(existing -> updateProfile(existing, profile))
                .orElseGet(() -> createMember(provider, profile));

        Map<String, Object> attributes = new HashMap<>(oauthUser.getAttributes());
        attributes.put("username", member.getUsername());
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(member.getRole().getValue())),
                attributes,
                "username"
        );
    }

    private Member updateProfile(Member member, SocialProfile profile) {
        member.updateSocialProfile(uniqueEmail(profile.email(), member), profile.nickname(), profile.profileImageUrl());
        return member;
    }

    private Member createMember(String provider, SocialProfile profile) {
        LoginType loginType = LoginType.valueOf(provider);
        return memberRepository.save(Member.builder()
                .username(createUsername(provider, profile.providerId()))
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .email(uniqueEmail(profile.email(), null, provider, profile.providerId()))
                .nickname(defaultNickname(profile.nickname(), provider))
                .loginType(loginType)
                .provider(provider)
                .providerId(profile.providerId())
                .profileImageUrl(profile.profileImageUrl())
                .role(MemberRole.ROLE_USER)
                .mannerScore(36.5)
                .build());
    }

    private SocialProfile extractProfile(String provider, Map<String, Object> attributes) {
        if ("KAKAO".equals(provider)) {
            Map<String, Object> account = castMap(attributes.get("kakao_account"));
            Map<String, Object> profile = castMap(account.get("profile"));
            return new SocialProfile(
                    requiredProviderId(provider, attributes.get("id")),
                    stringValue(account.get("email")),
                    stringValue(profile.get("nickname")),
                    stringValue(profile.get("profile_image_url"))
            );
        }
        if ("NAVER".equals(provider)) {
            Map<String, Object> response = castMap(attributes.get("response"));
            return new SocialProfile(
                    requiredProviderId(provider, response.get("id")),
                    stringValue(response.get("email")),
                    stringValue(response.get("nickname")),
                    stringValue(response.get("profile_image"))
            );
        }
        return new SocialProfile(
                requiredProviderId(provider, attributes.get("sub")),
                stringValue(attributes.get("email")),
                stringValue(attributes.get("name")),
                stringValue(attributes.get("picture"))
        );
    }

    private String requiredProviderId(String provider, Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            throw new OAuth2AuthenticationException(provider + " 사용자 식별자를 찾을 수 없습니다.");
        }
        return String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return value instanceof Map<?, ?> ? (Map<String, Object>) value : Map.of();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String createUsername(String provider, String providerId) {
        String base = provider.toLowerCase() + "_" + Math.abs(providerId.hashCode());
        String username = base;
        int suffix = 1;
        while (memberRepository.existsByUsername(username)) {
            username = base + "_" + suffix++;
        }
        return username;
    }

    private String defaultNickname(String nickname, String provider) {
        return nickname == null || nickname.isBlank() ? provider + " 사용자" : nickname;
    }

    private String uniqueEmail(String email, Member owner) {
        return uniqueEmail(email, owner, owner.getProvider(), owner.getProviderId());
    }

    private String uniqueEmail(String email, Member owner, String provider, String providerId) {
        String fallback = provider.toLowerCase() + "_" + Math.abs(providerId.hashCode()) + "@social.neighbortrade.local";
        if (email == null || email.isBlank()) return fallback;
        return memberRepository.findByEmail(email)
                .filter(found -> owner == null || !found.getId().equals(owner.getId()))
                .map(found -> fallback)
                .orElse(email);
    }

    private record SocialProfile(String providerId, String email, String nickname, String profileImageUrl) {
    }
}
