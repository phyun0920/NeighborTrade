package com.study.neighbortrade.domain.member;

import com.study.neighbortrade.domain.location.Neighborhood;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "member",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_member_provider_provider_id",
                columnNames = {"provider", "provider_id"}
        )
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoginType loginType;

    @Column(length = 30)
    private String provider;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)

    @Column(nullable = false, length = 30)
    private MemberRole role;

    @Column(nullable = false)
    private boolean localVerified;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "verified_neighborhood_id")
    private Neighborhood verifiedNeighborhood;

    @Column(nullable = false)
    private double mannerScore;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (role == null) role = MemberRole.ROLE_USER;
        if (loginType == null) loginType = LoginType.LOCAL;
        if (nickname == null || nickname.isBlank()) nickname = username;
        if (mannerScore == 0) mannerScore = 36.5;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    public void verifyLocal(Neighborhood neighborhood) {
        verifiedNeighborhood = neighborhood;
        localVerified = true;
        if (role == MemberRole.ROLE_USER) role = MemberRole.ROLE_LOCAL_VERIFIED;
    }
    public void updateMannerScore(double mannerScore) {
        this.mannerScore = mannerScore;
    }

    public void updateSocialProfile(String email, String nickname, String profileImageUrl) {
        if (email != null && !email.isBlank()) this.email = email;
        if (nickname != null && !nickname.isBlank()) this.nickname = nickname;
        if (profileImageUrl != null && !profileImageUrl.isBlank()) this.profileImageUrl = profileImageUrl;
    }
}
