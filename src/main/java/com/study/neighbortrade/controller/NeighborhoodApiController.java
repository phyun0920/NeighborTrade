package com.study.neighbortrade.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.study.neighbortrade.domain.location.Neighborhood;
import com.study.neighbortrade.repository.NeighborhoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class NeighborhoodApiController {

    private final NeighborhoodRepository neighborhoodRepository;

    @GetMapping("/api/neighborhoods")
    public List<NeighborhoodOptionJson> listNeighborhoods() {
        return neighborhoodRepository.findAll().stream()
                .sorted(Comparator.comparing(Neighborhood::getDisplayName))
                .map(n -> new NeighborhoodOptionJson(
                        n.getId(),
                        n.getDisplayName(),
                        n.getEmdName(),
                        n.getSido(),
                        n.getSigungu()))
                .toList();
    }

    /** JSON에는 동 이름을 {@code townName}으로 내려 프론트·문서와 필드명을 맞춤({@code emdName}과 동일 값). */
    public static record NeighborhoodOptionJson(
            Long id,
            String displayName,
            @JsonProperty("townName") String emdName,
            String sido,
            String sigungu) {}
}
