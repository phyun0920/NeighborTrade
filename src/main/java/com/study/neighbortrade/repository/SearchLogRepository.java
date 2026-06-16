package com.study.neighbortrade.repository;

import com.study.neighbortrade.domain.search.SearchLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    @EntityGraph(attributePaths = {"browsingNeighborhood", "member"})
    Page<SearchLog> findAllByOrderBySearchedAtDesc(Pageable pageable);

    @Query("SELECT COUNT(sl) FROM SearchLog sl WHERE sl.searchedAt >= :since")
    long countBySearchedAtAfter(@Param("since") LocalDateTime since);
    /**
     * 주간 top N 인기검색어 조회
     * @param startDate 집계 시작일시
     * @param pageable top N 제한
     * @return keyword와 count를 담은 객체 리스트 (Object[0]=keyword, Object[1]=count)
     */
    @Query("SELECT sl.keyword, COUNT(sl) as cnt " +
            "FROM SearchLog sl " +
            "WHERE sl.searchedAt >= :startDate AND sl.searchedAt < :endDate " +
            "GROUP BY sl.keyword " +
            "ORDER BY cnt DESC")
    List<Object[]> findTopKeywordsByPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 오래된 로그 삭제 (retention policy)
     * @param beforeDate 이 날짜 이전 로그 삭제
     */
    @Modifying
    @Query("DELETE FROM SearchLog sl WHERE sl.searchedAt < :beforeDate")
    void deleteOlderThan(@Param("beforeDate") LocalDateTime beforeDate);

    /** 특정 키워드의 검색 횟수 조회 */
    @Query("SELECT COUNT(sl) FROM SearchLog sl WHERE sl.keyword = :keyword AND sl.searchedAt >= :startDate")
    long countByKeywordAndDateAfter(@Param("keyword") String keyword, @Param("startDate") LocalDateTime startDate);
}
