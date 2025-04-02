package com.kanevsky.stats.repos;

import com.kanevsky.stats.model.AverageCalculator;
import com.kanevsky.stats.model.Stats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatsRepo {
    private Map<String, AverageCalculator> statsMap = new ConcurrentHashMap<>();

    public void accept(String key, Stats singleGameStats) {
        statsMap.compute(key, (k, average) -> {
            if (average == null) {
                average = new AverageCalculator();
            }
            average.accept(singleGameStats);
            return average;
        });
    }

    public Stats getStats(String key){
        AverageCalculator calculator = statsMap.get(key);
        if (calculator != null) {
            return calculator.getAverage();
        }

        return null;
    }
}
