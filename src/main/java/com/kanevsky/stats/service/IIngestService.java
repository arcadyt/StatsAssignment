package com.kanevsky.stats.service;

import com.kanevsky.stats.dto.StatsEntryDto;

import java.util.List;

public interface IIngestService {
    boolean processStatsEntry(StatsEntryDto statsEntry);

    int processBatchEntries(List<StatsEntryDto> entries);
}
