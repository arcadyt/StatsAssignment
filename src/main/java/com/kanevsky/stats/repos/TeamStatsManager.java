package com.kanevsky.stats.repos;

import com.kanevsky.stats.model.Stats;

public class TeamStatsManager implements IStatsManager {

    private StatsRepo teamsRepo = new StatsRepo();

    @Override
    public boolean submit(Stats gameStats, String name) {
        try {
            teamsRepo.accept(name, gameStats);
            return true;

        }catch (Exception e){
            return false;
        }
    }
    @Override
    public Stats getStats(String name) {
        return teamsRepo.getStats(name);
    }
}
