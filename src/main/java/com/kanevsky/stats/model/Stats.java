package com.kanevsky.stats.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stats {
    double points;
    double rebounds;
    double assists;
    double steals;
    double blocks;
    double fouls;
    double turnovers;
    double minutesPlayed;

    Stats multiply(double scalar){
        return new Stats(points*scalar, rebounds*scalar, assists*scalar, steals*scalar, blocks*scalar, fouls*scalar, turnovers*scalar,
                minutesPlayed*scalar);
    }

    Stats divide(double scalar){
        return multiply(1/scalar);
    }

    Stats add(Stats newStats){
        return new Stats(points + newStats.points, rebounds + newStats.rebounds, assists + newStats.assists,
                steals + newStats.steals, blocks + newStats.blocks, fouls + newStats.fouls, turnovers + newStats.turnovers,
                minutesPlayed + newStats.minutesPlayed);
    }
}
