package com.study.neighbortrade.web;

import com.study.neighbortrade.domain.location.Neighborhood;
import com.study.neighbortrade.repository.NeighborhoodRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BrowsingNeighborhoodResolver {

    public static final String COOKIE_NAME = "browse_neighborhood_id";
    public static final String QUERY_PARAM = "nh";

    private final NeighborhoodRepository neighborhoodRepository;

    /**
     * URL {@code nh} 우선, 없으면 쿠키 {@link #COOKIE_NAME}. 유효한 행만 반환.
     */
    public Optional<Neighborhood> resolve(HttpServletRequest request) {
        String nh = request.getParameter(QUERY_PARAM);
        if (nh != null && !nh.isBlank()) {
            Optional<Neighborhood> byParam = parseAndFind(nh);
            if (byParam.isPresent()) {
                return byParam;
            }
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie c : cookies) {
            if (COOKIE_NAME.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                return parseAndFind(c.getValue());
            }
        }
        return Optional.empty();
    }

    private Optional<Neighborhood> parseAndFind(String raw) {
        try {
            long id = Long.parseLong(raw.trim());
            return neighborhoodRepository.findById(id);
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }
}
