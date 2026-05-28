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
import java.util.function.Supplier;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@Order(100)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final MemberRepository memberRepository;
    private final NeighborhoodRepository neighborhoodRepository;
    private final ProductPostRepository productPostRepository;
    private final ProductFavoriteRepository productFavoriteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Neighborhood gasan = ensureNeighborhood(
                "서울특별시 금천구 가산동",
                () -> Neighborhood.builder().sido("서울특별시").sigungu("금천구").emdName("가산동")
                        .displayName("서울특별시 금천구 가산동").centerLatitude(37.4767).centerLongitude(126.8830)
                        .verifyRadiusMeters(2500).build());
        List.of(
                neighborhood("서울특별시", "금천구", "독산동", "서울특별시 금천구 독산동", 37.4696, 126.8974),
                neighborhood("서울특별시", "금천구", "시흥동", "서울특별시 금천구 시흥동", 37.4550, 126.9000),
                neighborhood("서울특별시", "금천구", "독산제1동", "서울특별시 금천구 독산제1동", 37.4706, 126.8896),
                neighborhood("서울특별시", "금천구", "시흥제1동", "서울특별시 금천구 시흥제1동", 37.4536, 126.9032)
        ).forEach(n -> ensureNeighborhood(n.getDisplayName(), () -> n));

        memberRepository.findByUsername("admin").orElseGet(() -> memberRepository.save(Member.builder()
                .username("admin").password(passwordEncoder.encode("admin1234")).email("admin@neighbortrade.local")
                .nickname("관리자").role(MemberRole.ROLE_ADMIN).localVerified(true).verifiedNeighborhood(gasan)
                .mannerScore(40.0).build()));
        Member seller = memberRepository.findByUsername("seller").orElseGet(() -> memberRepository.save(Member.builder()
                .username("seller").password(passwordEncoder.encode("seller1234")).email("seller@neighbortrade.local")
                .nickname("가산이웃").role(MemberRole.ROLE_LOCAL_VERIFIED).localVerified(true).verifiedNeighborhood(gasan)
                .mannerScore(38.5).build()));
        Member buyer = memberRepository.findByUsername("buyer").orElseGet(() -> memberRepository.save(Member.builder()
                .username("buyer").password(passwordEncoder.encode("buyer1234")).email("buyer@neighbortrade.local")
                .nickname("가산구매자").role(MemberRole.ROLE_LOCAL_VERIFIED).localVerified(true).verifiedNeighborhood(gasan)
                .mannerScore(37.0).build()));

        if (productPostRepository.count() == 0) {
            Neighborhood docksan = ensureNeighborhood("서울특별시 금천구 독산동", () -> neighborhood("서울특별시", "금천구", "독산동", "서울특별시 금천구 독산동", 37.4696, 126.8974));
            Neighborhood siheung = ensureNeighborhood("서울특별시 금천구 시흥동", () -> neighborhood("서울특별시", "금천구", "시흥동", "서울특별시 금천구 시흥동", 37.4550, 126.9000));
            Neighborhood docksan1 = ensureNeighborhood("서울특별시 금천구 독산제1동", () -> neighborhood("서울특별시", "금천구", "독산제1동", "서울특별시 금천구 독산제1동", 37.4706, 126.8896));
            Neighborhood siheung1 = ensureNeighborhood("서울특별시 금천구 시흥제1동", () -> neighborhood("서울특별시", "금천구", "시흥제1동", "서울특별시 금천구 시흥제1동", 37.4536, 126.9032));

            savePost(seller, gasan, "깨끗한 책상 판매합니다", ProductCategory.FURNITURE, 20000, false,
                    "가산동에서 직거래 가능한 책상입니다. 사용감은 적습니다.", "가산디지털단지역 근처");
            savePost(seller, gasan, "모니터 나눔", ProductCategory.DIGITAL, 0, true,
                    "작동 확인했습니다. 직접 가져가실 분께 나눔합니다.", "가산동 주민센터 근처");
            savePost(seller, docksan, "독산동 에어컨", ProductCategory.APPLIANCE, 150000, false,
                    "여름 대비 창문형 에어컨입니다. 1년 사용.", "독산역 근처");
            savePost(seller, docksan, "유모차 판매", ProductCategory.KIDS, 80000, false,
                    "깨끗하게 사용했습니다. 직거래 우선.", "독산동 로타리 근처");
            savePost(seller, siheung, "자전거", ProductCategory.SPORTS, 120000, false,
                    "21단 로드 자전거입니다. 정기 점검했습니다.", "시흥역 근처");
            savePost(seller, siheung, "아이폰 13 케이스", ProductCategory.DIGITAL, 5000, false,
                    "미사용 새 제품입니다.", "시흥동 마트 앞");
            savePost(seller, docksan1, "캠핑 의자 2개", ProductCategory.SPORTS, 30000, false,
                    "접이식 캠핑 의자 2개 세트입니다.", "독산제1동 주민센터");
            savePost(seller, docksan1, "식물 화분", ProductCategory.PLANT, 15000, false,
                    "몬스테라 분갈이 화분입니다.", "독산제1동 골목");
            savePost(seller, siheung1, "전공 서적 5권", ProductCategory.BOOK, 25000, false,
                    "컴퓨터공학 전공 서적입니다.", "시흥제1동 도서관 근처");
            savePost(seller, siheung1, "강아지 하네스", ProductCategory.PET, 10000, false,
                    "소형견용 하네스입니다.", "시흥제1동 공원");
        }

        if (productFavoriteRepository.count() == 0) {
            productPostRepository.findAll().stream().findFirst().ifPresent(post ->
                    productFavoriteRepository.save(ProductFavorite.builder().member(buyer).productPost(post).build()));
        }
    }

    private Neighborhood ensureNeighborhood(String displayName, Supplier<Neighborhood> creator) {
        return neighborhoodRepository.findByDisplayName(displayName).orElseGet(() -> neighborhoodRepository.save(creator.get()));
    }

    private static Neighborhood neighborhood(String sido, String sigungu, String emdName, String displayName,
                                             double lat, double lon) {
        return Neighborhood.builder().sido(sido).sigungu(sigungu).emdName(emdName).displayName(displayName)
                .centerLatitude(lat).centerLongitude(lon).verifyRadiusMeters(2500).build();
    }

    private void savePost(Member seller, Neighborhood neighborhood, String title, ProductCategory category,
                          int price, boolean giveaway, String content, String tradePlace) {
        productPostRepository.save(ProductPost.builder().seller(seller).neighborhood(neighborhood).title(title)
                .category(category).price(price).giveaway(giveaway).content(content)
                .representativeImageUrl("/images/placeholder-product.svg").tradePlace(tradePlace)
                .status(ProductStatus.ON_SALE).build());
    }
}
