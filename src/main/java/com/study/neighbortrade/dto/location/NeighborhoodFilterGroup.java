package com.study.neighbortrade.dto.location;

import com.study.neighbortrade.domain.location.Neighborhood;

import java.util.List;

public record NeighborhoodFilterGroup(String label, List<Neighborhood> neighborhoods) {}
