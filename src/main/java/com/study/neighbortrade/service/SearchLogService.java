package com.study.neighbortrade.service;

import com.study.neighbortrade.domain.location.Neighborhood;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.search.SearchLog;
import com.study.neighbortrade.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;

    /**
     * 검색 기록 저장
     * @param keyword 검색 키워드
     * @param browsingNeighborhood 검색 시점의 동네 (null 가능)
     * @param member 검색한 사용자 (null 가능 = 익명)
     * @param productCount 검색 결과 개수
     */
    public void logSearch(String keyword, Neighborhood browsingNeighborhood, Member member, int productCount) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        SearchLog searchLog = SearchLog.of(
                keyword.trim(),
                browsingNeighborhood,
                member,
                productCount
        );

        searchLogRepository.save(searchLog);
        log.debug("Search logged: keyword={}, browsingNh={}, member={}, count={}",
                keyword, browsingNeighborhood != null ? browsingNeighborhood.getId() : null,
                member != null ? member.getId() : null, productCount);
    }

    /**
     * 주간 top N 인기검색어 조회
     * @param limit top N
     * @return keyword 리스트
     */
    @Transactional(readOnly = true)
    public List<String> getTopKeywordsByWeek(int limit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        List<Object[]> results = searchLogRepository.findTopKeywordsByPeriod(weekAgo, now, PageRequest.of(0, limit));

        return results.stream()
                .map(obj -> (String) obj[0])
                .toList();
    }

    /**
     * 특정 기간의 top N 인기검색어
     */
    @Transactional(readOnly = true)
    public List<Object[]> getTopKeywordsByPeriod(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        return searchLogRepository.findTopKeywordsByPeriod(startDate, endDate, PageRequest.of(0, limit));
    }

    /**
     * 오래된 검색 로그 삭제 (retention policy)
     * @param daysToKeep 유지할 일 수
     */
    public void cleanupOldLogs(int daysToKeep) {
        LocalDateTime beforeDate = LocalDateTime.now().minusDays(daysToKeep);
        searchLogRepository.deleteOlderThan(beforeDate);
        log.info("Old search logs deleted before: {}", beforeDate);
    }

    /**
     * 특정 키워드의 최근 일주일 검색 횟수
     */
    @Transactional(readOnly = true)
    public long getKeywordCountThisWeek(String keyword) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        return searchLogRepository.countByKeywordAndDateAfter(keyword, weekAgo);
    }
}
