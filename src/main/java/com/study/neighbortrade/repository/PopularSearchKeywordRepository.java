package com.study.neighbortrade.repository;

import com.study.neighbortrade.domain.search.PopularSearchKeyword;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PopularSearchKeywordRepository extends JpaRepository<PopularSearchKeyword, Long> {

    /**
     * 현재 주간(period)의 모든 인기검색어 조회 (rank 순서)
     */
    @Query("SELECT psk FROM PopularSearchKeyword psk WHERE psk.period = :period ORDER BY psk.rank ASC")
    List<PopularSearchKeyword> findByPeriodOrderByRank(@Param("period") LocalDate period);

    /**
     * 특정 주간 상위 N개 조회
     */
    @Query("SELECT psk FROM PopularSearchKeyword psk WHERE psk.period = :period ORDER BY psk.rank ASC")
    List<PopularSearchKeyword> findByPeriodOrderByRank(@Param("period") LocalDate period, Pageable pageable);

    /**
     * 가장 최신 period의 top N 조회
     */
    @Query("SELECT psk FROM PopularSearchKeyword psk " +
            "WHERE psk.period = (SELECT MAX(psk2.period) FROM PopularSearchKeyword psk2) " +
            "ORDER BY psk.rank ASC")
    List<PopularSearchKeyword> findLatestTop(Pageable pageable);

    /**
     * 특정 키워드 + period로 조회
     */
    Optional<PopularSearchKeyword> findByKeywordAndPeriod(String keyword, LocalDate period);

    /**
     * 특정 period의 모든 데이터 조회
     */
    List<PopularSearchKeyword> findByPeriod(LocalDate period);

    /**
     * 오래된 period 데이터 삭제
     */
    @Modifying
    @Query("DELETE FROM PopularSearchKeyword psk WHERE psk.period < :beforeDate")
    void deleteOlderThanPeriod(@Param("beforeDate") LocalDate beforeDate);

    /**
     * 특정 period의 데이터 개수
     */
    long countByPeriod(LocalDate period);
}
