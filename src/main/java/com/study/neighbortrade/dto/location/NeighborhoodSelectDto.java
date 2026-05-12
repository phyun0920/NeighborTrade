package com.study.neighbortrade.dto.location;

public record NeighborhoodSelectDto(
        Long id,
        String displayName,
        double centerLatitude,
        double centerLongitude,
        int verifyRadiusMeters,
        boolean hasBoundary
) {}
