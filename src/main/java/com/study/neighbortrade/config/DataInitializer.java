package com.study.neighbortrade.config;

import com.study.neighbortrade.domain.location.Neighborhood;
import com.study.neighbortrade.domain.member.*;
import com.study.neighbortrade.domain.product.*;
import com.study.neighbortrade.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@Order(100)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final MemberRepository memberRepository;
    private final NeighborhoodRepository neighborhoodRepository;
    private final ProductPostRepository productPostRepository;
    private final PasswordEncoder passwordEncoder;

    @Override

    @Transactional
    public void run(String... args) {
        Neighborhood gasan = neighborhoodRepository.findByDisplayName("서울특별시 금천구 가산동").orElseGet(() -> neighborhoodRepository.save(Neighborhood.builder().sido("서울특별시").sigungu("금천구").emdName("가산동").displayName("서울특별시 금천구 가산동").centerLatitude(37.4767).centerLongitude(126.8830).verifyRadiusMeters(2500).build()));
        List.of( Neighborhood.builder().sido("서울특별시").sigungu("금천구").emdName("독산동").displayName("서울특별시 금천구 독산동").centerLatitude(37.4696).centerLongitude(126.8974).verifyRadiusMeters(2500).build(), Neighborhood.builder().sido("서울특별시").sigungu("금천구").emdName("시흥동").displayName("서울특별시 금천구 시흥동").centerLatitude(37.4550).centerLongitude(126.9000).verifyRadiusMeters(2500).build(), Neighborhood.builder().sido("서울특별시").sigungu("금천구").emdName("독산제1동").displayName("서울특별시 금천구 독산제1동").centerLatitude(37.4706).centerLongitude(126.8896).verifyRadiusMeters(2500).build(), Neighborhood.builder().sido("서울특별시").sigungu("금천구").emdName("시흥제1동").displayName("서울특별시 금천구 시흥제1동").centerLatitude(37.4536).centerLongitude(126.9032).verifyRadiusMeters(2500).build() ).forEach(n -> neighborhoodRepository.findByDisplayName(n.getDisplayName()).orElseGet(() -> neighborhoodRepository.save(n)));
        memberRepository.findByUsername("admin").orElseGet(() -> memberRepository.save(Member.builder().username("admin").password(passwordEncoder.encode("admin1234")).email("admin@neighbortrade.local").nickname("관리자").role(MemberRole.ROLE_ADMIN).localVerified(true).verifiedNeighborhood(gasan).mannerScore(40.0).build()));
        Member seller = memberRepository.findByUsername("seller").orElseGet(() -> memberRepository.save(Member.builder().username("seller").password(passwordEncoder.encode("seller1234")).email("seller@neighbortrade.local").nickname("가산이웃").role(MemberRole.ROLE_LOCAL_VERIFIED).localVerified(true).verifiedNeighborhood(gasan).mannerScore(38.5).build()));
        if (productPostRepository.count() == 0) {
            productPostRepository.save(ProductPost.builder().seller(seller).neighborhood(gasan).title("깨끗한 책상 판매합니다").category(ProductCategory.FURNITURE).price(20000).content("가산동에서 직거래 가능한 책상입니다. 사용감은 적습니다.").representativeImageUrl("/images/placeholder-product.svg").tradePlace("가산디지털단지역 근처").status(ProductStatus.ON_SALE).build());
            productPostRepository.save(ProductPost.builder().seller(seller).neighborhood(gasan).title("모니터 나눔").category(ProductCategory.DIGITAL).price(0).giveaway(true).content("작동 확인했습니다. 직접 가져가실 분께 나눔합니다.").representativeImageUrl("/images/placeholder-product.svg").tradePlace("가산동 주민센터 근처").status(ProductStatus.ON_SALE).build());
        }
    }
}
