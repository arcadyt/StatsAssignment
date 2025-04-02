package com.kanevsky.stats.service;

import com.kanevsky.stats.dto.StatsEntryDto;
import com.kanevsky.stats.mappers.IStatsMapper;
import com.kanevsky.stats.model.Stats;
import com.kanevsky.stats.repos.IStatsManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class IngestService {

    @Autowired
    private IStatsMapper statsMapper;

    @Autowired
    @Qualifier("playerStatsManager")
    private IStatsManager playerStatsManager;

    @Autowired
    @Qualifier("teamStatsManager")
    private IStatsManager teamStatsManager;

    /**
     * Process a single stats entry
     */
    public boolean processStatsEntry(StatsEntryDto statsEntry) {
        try {
            Stats stats = statsMapper.toStats(statsEntry.getStats());
            
            boolean playerSuccess = playerStatsManager.submit(stats, statsEntry.getPlayerName());
            boolean teamSuccess = teamStatsManager.submit(stats, statsEntry.getTeamName());
            
            return playerSuccess && teamSuccess;
        } catch (Exception e) {
            log.error("Error processing stats entry for player: {}, team: {}", 
                    statsEntry.getPlayerName(), statsEntry.getTeamName(), e);
            return false;
        }
    }

    /**
     * Process a batch of stats entries
     */
    public int processBatchEntries(List<StatsEntryDto> entries) {
        if (entries == null || entries.isEmpty()) {
            return 0;
        }

        AtomicInteger successCount = new AtomicInteger(0);
        
        entries.forEach(entry -> {
            if (processStatsEntry(entry)) {
                successCount.incrementAndGet();
            }
        });
        
        return successCount.get();
    }
}