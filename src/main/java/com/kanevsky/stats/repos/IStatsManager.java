package com.kanevsky.stats.repos;

import com.kanevsky.stats.model.Stats;

public interface IStatsManager {

    boolean submit(Stats stats, String name);
    Stats getStats(String name);
}
