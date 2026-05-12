package com.study.neighbortrade.service;

import com.study.neighbortrade.domain.location.LocationVerification;
import com.study.neighbortrade.domain.location.Neighborhood;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.dto.location.LocationVerifyRequestDto;
import com.study.neighbortrade.repository.LocationVerificationRepository;
import com.study.neighbortrade.repository.MemberRepository;
import com.study.neighbortrade.repository.NeighborhoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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
