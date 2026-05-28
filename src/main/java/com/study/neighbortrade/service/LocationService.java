package com.study.neighbortrade.service;

import com.study.neighbortrade.domain.location.LocationVerification;
import com.study.neighbortrade.domain.location.Neighborhood;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.dto.location.LocationVerifyRequestDto;
import com.study.neighbortrade.dto.location.NeighborhoodFilterGroup;
import com.study.neighbortrade.dto.location.NeighborhoodSelectDto;
import com.study.neighbortrade.repository.LocationVerificationRepository;
import com.study.neighbortrade.repository.MemberRepository;
import com.study.neighbortrade.repository.NeighborhoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {
    private final NeighborhoodRepository neighborhoodRepository;
    private final LocationVerificationRepository verificationRepository;
    private final MemberRepository memberRepository;

    @Value("${app.postgis.enabled:false}")
    private boolean postgisEnabled;

    public List<Neighborhood> findAllNeighborhoods() {
        return neighborhoodRepository.findAll();
    }

    public Optional<Neighborhood> findNeighborhoodById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return neighborhoodRepository.findById(id);
    }

    /** `/api/neighborhoods`와 동일 데이터를 구(sigungu) 단위로 묶어 sidebar 필터에 사용 */
    public List<NeighborhoodFilterGroup> findNeighborhoodFilterGroups() {
        Map<String, List<Neighborhood>> grouped = neighborhoodRepository.findAll().stream()
                .sorted(Comparator.comparing(Neighborhood::getSigungu).thenComparing(Neighborhood::getEmdName))
                .collect(Collectors.groupingBy(
                        n -> n.getSido() + " " + n.getSigungu(),
                        LinkedHashMap::new,
                        Collectors.toList()));
        return grouped.entrySet().stream()
                .map(entry -> new NeighborhoodFilterGroup(entry.getKey(), entry.getValue()))
                .toList();
    }

    // 서버에서 동네별 경계여부 내려주기 : postgis 켜짐 그리고 선택 동네에 경계 있을 때만 문구 표시 추가(20260512)
    public List<NeighborhoodSelectDto> findAllNeighborhoodsForSelect() {
        List<Neighborhood> all = neighborhoodRepository.findAll();
        Map<Long, Boolean> boundaryById = Map.of();
        if (postgisEnabled) {
            boundaryById = new HashMap<>();
            for (Object[] row : neighborhoodRepository.findAllBoundaryPresence()) {
                Long id = ((Number) row[0]).longValue();
                boolean hasB = row[1] instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(row[1]));
                boundaryById.put(id, hasB);
            }
        }
        Map<Long, Boolean> flags = boundaryById;
        return all.stream()
                .map(n -> new NeighborhoodSelectDto(
                        n.getId(),
                        n.getDisplayName(),
                        n.getCenterLatitude(),
                        n.getCenterLongitude(),
                        n.getVerifyRadiusMeters(),
                        flags.getOrDefault(n.getId(), false)))
                .sorted(Comparator.comparing(NeighborhoodSelectDto::displayName))
                .toList();
    }

    @Transactional
    public boolean verify(Member member, LocationVerifyRequestDto dto) {
        Neighborhood neighborhood = neighborhoodRepository.findById(dto.getNeighborhoodId()).orElseThrow(() -> new IllegalArgumentException("동네를 찾을 수 없습니다."));
        double distance = distanceMeters(dto.getLatitude(), dto.getLongitude(), neighborhood.getCenterLatitude(), neighborhood.getCenterLongitude());
        boolean verified = verifyByPostgisOrRadius(neighborhood, dto, distance);
        String failureReason = verified ? null : "선택한 동네 인증 범위 밖입니다.";
        verificationRepository.save(LocationVerification.builder().member(member).neighborhood(neighborhood).latitude(dto.getLatitude()).longitude(dto.getLongitude()).verified(verified).failureReason(failureReason).build());
        if (verified) {
            member.verifyLocal(neighborhood);
            memberRepository.save(member);
        }
        return verified;
    }
    private double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private boolean verifyByPostgisOrRadius(Neighborhood neighborhood, LocationVerifyRequestDto dto, double distance) {
        if (postgisEnabled && neighborhoodRepository.hasBoundary(neighborhood.getId())) {
            return neighborhoodRepository.containsPoint(neighborhood.getId(), dto.getLatitude(), dto.getLongitude());
        }
        return distance <= neighborhood.getVerifyRadiusMeters();
    }
}
