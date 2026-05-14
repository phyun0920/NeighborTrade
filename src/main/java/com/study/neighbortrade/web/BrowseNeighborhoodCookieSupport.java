package com.study.neighbortrade.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

public final class BrowseNeighborhoodCookieSupport {

    private BrowseNeighborhoodCookieSupport() {}

    public static void writeBrowsingNeighborhoodCookie(HttpServletResponse response, long neighborhoodId) {
        ResponseCookie cookie =
                ResponseCookie.from(BrowsingNeighborhoodResolver.COOKIE_NAME, String.valueOf(neighborhoodId))
                        .path("/")
                        .maxAge(Duration.ofDays(180))
                        .httpOnly(true)
                        .sameSite("Lax")
                        .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void clearBrowsingNeighborhoodCookie(HttpServletResponse response) {
        ResponseCookie cookie =
                ResponseCookie.from(BrowsingNeighborhoodResolver.COOKIE_NAME, "")
                        .path("/")
                        .maxAge(0)
                        .httpOnly(true)
                        .sameSite("Lax")
                        .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
