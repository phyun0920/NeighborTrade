package com.study.neighbortrade.repository;

import com.study.neighbortrade.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 20260515금 관리자메뉴 구현에서 회원관리 표에 넣을 컬럼 리스트
    @Query("select m from Member m left join fetch m.verifiedNeighborhood order by m.id asc")
    List<Member> findAllWithVerifiedNeighborhood();
    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByProviderAndProviderId(String provider, String providerId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
