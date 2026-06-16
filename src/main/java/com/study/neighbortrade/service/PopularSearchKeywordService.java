package com.study.neighbortrade.service;

import com.study.neighbortrade.domain.search.PopularSearchKeyword;
import com.study.neighbortrade.config.MarketProperties;
import com.study.neighbortrade.repository.PopularSearchKeywordRepository;
import com.study.neighbortrade.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PopularSearchKeywordService {

    private final PopularSearchKeywordRepository popularSearchKeywordRepository;
    private final SearchLogRepository searchLogRepository;
    private final MarketProperties marketProperties;

    @Value("${app.market.popular-keywords-limit:10}")
    private int popularKeywordsLimit;

    /**
     * 최신 주간 인기검색어 top N 조회
     * DB에 없으면 YAML 폴백 사용
     */
    @Transactional(readOnly = true)
    public List<String> getPopularKeywords() {
        List<PopularSearchKeyword> keywords = popularSearchKeywordRepository.findLatestTop(PageRequest.of(0, popularKeywordsLimit));

        if (!keywords.isEmpty()) {
            return keywords.stream()
                    .map(PopularSearchKeyword::getKeyword)
                    .toList();
        }

        log.debug("No popular keywords in DB, using fallback from application config");
        return marketProperties.popularKeywords();
    }

    /**
     * 특정 주간(period)의 인기검색어 조회
     */
    @Transactional(readOnly = true)
    public List<PopularSearchKeyword> getPopularKeywordsByPeriod(LocalDate period) {
        return popularSearchKeywordRepository.findByPeriodOrderByRank(period);
    }

    /**
     * 주간 집계 배치: SearchLog에서 top N 키워드를 집계하여 PopularSearchKeyword 저장
     * 매주 월요일 00:00에 실행 (이전 주간 데이터 집계)
     *
     * 현재 구현: DELETE + INSERT (새로 시작)
     */
    public WeeklyAggregateResult aggregateWeekly() {
        // 이전 주간의 월요일 계산
        LocalDate today = LocalDate.now();
        LocalDate currentMonday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate lastMonday = currentMonday.minusWeeks(1);

        // 해당 주간 시작/종료 시간
        LocalDateTime weekStart = lastMonday.atStartOfDay();
        LocalDateTime weekEnd = lastMonday.plusDays(7).atStartOfDay();

        log.info("Aggregating popular keywords for period: {} to {}", weekStart, weekEnd);

        // SearchLog에서 top N 키워드 추�
        List<Object[]> topKeywords = searchLogRepository.findTopKeywordsByPeriod(weekStart, weekEnd, PageRequest.of(0, popularKeywordsLimit));

        // 기존 데이터 삭제
        popularSearchKeywordRepository.deleteOlderThanPeriod(lastMonday);

        // 새로운 데이터 저장
        IntStream.range(0, topKeywords.size())
                .forEach(index -> {
                    Object[] row = topKeywords.get(index);
                    String keyword = (String) row[0];
                    Long count = (Long) row[1];

                    PopularSearchKeyword psk = PopularSearchKeyword.of(
                            index + 1,  // rank: 1부터 시작
                            keyword,
                            count.intValue(),
                            lastMonday
                    );

                    popularSearchKeywordRepository.save(psk);
                    log.debug("Saved popular keyword: rank={}, keyword={}, count={}", index + 1, keyword, count);
                });

        log.info("Popular keywords aggregation completed: {} keywords saved", topKeywords.size());
        return new WeeklyAggregateResult(topKeywords.size(), lastMonday);
    }

    /**
     * [예제] UPSERT 방식: 성능 최적화 버전
     * - 기존 데이터 갱신 (UPDATE) + 새로운 데이터 추가 (INSERT)
     * - 트랜잭션 내 일괄 처리로 원자성 보장
     *
     * 사용법: aggregateWeeklyWithUpsert(LocalDate lastMonday);
     */
    @Transactional
    public void aggregateWeeklyWithUpsert() {
        // 이전 주간의 월요일 계산
        LocalDate today = LocalDate.now();
        LocalDate currentMonday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate lastMonday = currentMonday.minusWeeks(1);

        // 해당 주간 시작/종료 시간
        LocalDateTime weekStart = lastMonday.atStartOfDay();
        LocalDateTime weekEnd = lastMonday.plusDays(7).atStartOfDay();

        log.info("[UPSERT 방식] Aggregating popular keywords for period: {} to {}", weekStart, weekEnd);

        // SearchLog에서 top N 키워드 추출
        List<Object[]> topKeywords = searchLogRepository.findTopKeywordsByPeriod(weekStart, weekEnd, PageRequest.of(0, popularKeywordsLimit));

        // UPSERT 처리: 트랜잭션 내 일괄 처리
        LocalDateTime now = LocalDateTime.now();
        List<PopularSearchKeyword> keywordsToSave = new ArrayList<>();

        IntStream.range(0, topKeywords.size())
                .forEach(index -> {
                    Object[] row = topKeywords.get(index);
                    String keyword = (String) row[0];
                    Long count = (Long) row[1];

                    // 기존 데이터 확인 (SELECT)
                    var existing = popularSearchKeywordRepository.findByKeywordAndPeriod(keyword, lastMonday);

                    if (existing.isPresent()) {
                        // UPDATE: 기존 데이터 갱신
                        PopularSearchKeyword psk = existing.get();
                        psk.updateRank(index + 1);
                        psk.incrementCount(); // 또는: psk.setSearchCount(count.intValue());
                        log.debug("Updated popular keyword: rank={}, keyword={}, count={}", index + 1, keyword, count);
                        keywordsToSave.add(psk);
                    } else {
                        // INSERT: 새로운 데이터 생성
                        PopularSearchKeyword psk = PopularSearchKeyword.of(
                                index + 1,
                                keyword,
                                count.intValue(),
                                lastMonday
                        );
                        log.debug("Created new popular keyword: rank={}, keyword={}, count={}", index + 1, keyword, count);
                        keywordsToSave.add(psk);
                    }
                });

        // 일괄 저장 (배치 처리)
        if (!keywordsToSave.isEmpty()) {
            popularSearchKeywordRepository.saveAll(keywordsToSave);
            log.info("[UPSERT 방식] Aggregation completed: {} keywords saved/updated", keywordsToSave.size());
        }

        // 이번 주간 TOP N에 포함되지 않은 기존 데이터 제거 (선택사항)
        // 만약 rank가 변경되지 않은 항목이 있으면 유지, TOP N 외 항목만 삭제
        List<String> newKeywords = topKeywords.stream()
                .map(row -> (String) row[0])
                .toList();
        List<PopularSearchKeyword> toDelete = popularSearchKeywordRepository.findByPeriod(lastMonday).stream()
                .filter(psk -> !newKeywords.contains(psk.getKeyword()))
                .toList();
        if (!toDelete.isEmpty()) {
            popularSearchKeywordRepository.deleteAll(toDelete);
            log.debug("Deleted {} old popular keywords not in top N", toDelete.size());
        }
    }

    /**
     * [예제] Native Query를 이용한 UPSERT
     * PostgreSQL INSERT ... ON CONFLICT DO UPDATE 방식
     *
     * 장점: DB 레벨에서 원자적 처리, 성능 최적
     * 단점: Native Query이므로 DB 벤더에 종속
     */
    @Transactional
    public void aggregateWeeklyWithNativeUpsert() {
        LocalDate today = LocalDate.now();
        LocalDate currentMonday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate lastMonday = currentMonday.minusWeeks(1);

        LocalDateTime weekStart = lastMonday.atStartOfDay();
        LocalDateTime weekEnd = lastMonday.plusDays(7).atStartOfDay();

        log.info("[Native Query UPSERT 방식] Aggregating for period: {} to {}", weekStart, weekEnd);

        // SearchLog에서 top N 추출
        List<Object[]> topKeywords = searchLogRepository.findTopKeywordsByPeriod(weekStart, weekEnd, PageRequest.of(0, popularKeywordsLimit));

        // 각 키워드마다 UPSERT 쿼리 실행 (배치 처리 권장)
        // 실제 구현 시에는 PopularSearchKeywordRepository에 아래 메서드를 추가:
        /*
        @Query(value =
            "INSERT INTO popular_search_keyword (rank, keyword, search_count, period, created_at, updated_at) " +
            "VALUES (:rank, :keyword, :searchCount, :period, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
            "ON CONFLICT (keyword, period) DO UPDATE " +
            "  SET rank = :rank, " +
            "      search_count = :searchCount, " +
            "      updated_at = CURRENT_TIMESTAMP",
            nativeQuery = true)
        void upsertKeyword(
            @Param("rank") Integer rank,
            @Param("keyword") String keyword,
            @Param("searchCount") Integer searchCount,
            @Param("period") LocalDate period
        );
        */

        // 배치 실행 예제
        IntStream.range(0, topKeywords.size())
                .forEach(index -> {
                    Object[] row = topKeywords.get(index);
                    String keyword = (String) row[0];
                    Long count = (Long) row[1];

                    // upsertKeyword() 호출
                    // popularSearchKeywordRepository.upsertKeyword(index + 1, keyword, count.intValue(), lastMonday);
                    log.debug("Upserting (Native Query): rank={}, keyword={}, count={}", index + 1, keyword, count);
                });

        log.info("[Native Query UPSERT 방식] Completed");
    }


    /**
     * Admin: 인기검색어 생성 (선택적)
     */
    public PopularSearchKeyword createKeyword(Integer rank, String keyword, Integer searchCount, LocalDate period) {
        PopularSearchKeyword psk = PopularSearchKeyword.of(rank, keyword, searchCount, period);
        return popularSearchKeywordRepository.save(psk);
    }

    /**
     * Admin: 인기검색어 수정
     */
    public PopularSearchKeyword updateKeyword(Long id, String keyword) {
        PopularSearchKeyword psk = popularSearchKeywordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Popular keyword not found: " + id));
        psk.updateKeyword(keyword);
        return popularSearchKeywordRepository.save(psk);
    }

    /**
     * Admin: rank 수정
     */
    public PopularSearchKeyword updateRank(Long id, Integer rank) {
        PopularSearchKeyword psk = popularSearchKeywordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Popular keyword not found: " + id));
        psk.updateRank(rank);
        return popularSearchKeywordRepository.save(psk);
    }

    /**
     * Admin: 삭제
     */
    public void deleteKeyword(Long id) {
        popularSearchKeywordRepository.deleteById(id);
        log.info("Popular keyword deleted: id={}", id);
    }

    /**
     * Admin: 특정 주간 데이터 조회
     */
    @Transactional(readOnly = true)
    public List<PopularSearchKeyword> getByPeriod(LocalDate period) {
        return popularSearchKeywordRepository.findByPeriod(period);
    }
}
