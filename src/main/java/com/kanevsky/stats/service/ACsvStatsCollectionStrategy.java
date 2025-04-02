package com.kanevsky.stats.service;

import com.kanevsky.stats.model.Stats;
import com.kanevsky.stats.repos.IStatsManager;
import org.apache.commons.csv.CSVRecord;

public abstract class ACsvStatsCollectionStrategy {
    protected String keyName;

    protected IStatsManager statsManager;

    public String extractKey(CSVRecord record) {
        return record.get(keyName);
    }

    public void submit(String key, Stats stats) {
        statsManager.submit(stats, key);
    }
}
