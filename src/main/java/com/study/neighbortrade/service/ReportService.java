package com.study.neighbortrade.service;

import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.domain.report.Report;
import com.study.neighbortrade.domain.report.ReportStatus;
import com.study.neighbortrade.domain.report.ReportTargetType;
import com.study.neighbortrade.dto.report.ReportRequestDto;
import com.study.neighbortrade.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {
    private final ReportRepository reportRepository;

    @Transactional
    public Report create(Member reporter, ReportTargetType targetType, Long targetId, ReportRequestDto dto) {
        return reportRepository.save(Report.builder()
                .reporter(reporter)
                .targetType(targetType)
                .targetId(targetId)
                .reason(dto.getReason())
                .detail(dto.getDetail())
                .status(ReportStatus.RECEIVED)
                .build());
    }

    public Page<Report> list(int page) {
        return reportRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(page, 0), 20));
    }

    public Report findById(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));
    }

    @Transactional
    public void process(Long id, ReportStatus status, String processorNote) {
        findById(id).process(status, processorNote);
    }
}
