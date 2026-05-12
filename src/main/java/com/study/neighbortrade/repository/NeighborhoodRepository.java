package com.study.neighbortrade.repository;

import com.study.neighbortrade.domain.location.Neighborhood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
}
