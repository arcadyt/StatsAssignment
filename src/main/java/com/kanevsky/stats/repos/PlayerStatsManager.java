package com.kanevsky.stats.repos;

import com.kanevsky.stats.model.Stats;

public class PlayerStatsManager implements IStatsManager {

    private StatsRepo playersRepo = new StatsRepo();

    @Override
    public boolean submit(Stats gameStats, String name) {
        try {
            playersRepo.accept(name, gameStats);
            return true;

        }catch (Exception e){
            return false;
        }
    }
    @Override
    public Stats getStats(String name) {
        return playersRepo.getStats(name);
    }
}
