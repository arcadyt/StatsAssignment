package com.kanevsky.stats.repos;

import com.kanevsky.stats.model.Stats;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatsManager implements IStatsManager {

    private final StatsRepo statsRepo;

    @Override
    public boolean submit(Stats gameStats, String name) {
        if (gameStats == null || name == null || name.trim().isEmpty()) {
            return false;
        }

        try {
            statsRepo.accept(name, gameStats);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Stats getStats(String name) {
        return statsRepo.getStats(name);
    }
}