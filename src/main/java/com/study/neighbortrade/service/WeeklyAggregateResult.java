package com.study.neighbortrade.service;

import java.time.LocalDate;

public record WeeklyAggregateResult(int savedCount, LocalDate period) {}
