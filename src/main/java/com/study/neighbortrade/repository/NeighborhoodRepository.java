package com.study.neighbortrade.repository;

import com.study.neighbortrade.domain.location.Neighborhood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NeighborhoodRepository extends JpaRepository<Neighborhood, Long> {
    Optional<Neighborhood> findByDisplayName(String displayName);

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM neighborhood n
                WHERE n.id = :id
                  AND n.boundary IS NOT NULL
                  AND ST_Contains(n.boundary, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))
            )
            """, nativeQuery = true)
    boolean containsPoint(@Param("id") Long id, @Param("latitude") double latitude, @Param("longitude") double longitude);

    @Query(value = """
            SELECT n.boundary IS NOT NULL
            FROM neighborhood n
            WHERE n.id = :id
            """, nativeQuery = true)
    boolean hasBoundary(@Param("id") Long id);

    // 서버에서 동네별 경계여부 내려주기 : postgis 켜짐 그리고 선택 동네에 경계 있을 때만 문구 표시 추가(20260512)
    @Query(value = """
            SELECT n.id, (n.boundary IS NOT NULL)
            FROM neighborhood n
            """, nativeQuery = true)
    List<Object[]> findAllBoundaryPresence();
}
