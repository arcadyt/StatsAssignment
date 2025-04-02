package com.kanevsky.stats.service;

import com.kanevsky.stats.dto.PlayerStatsDto;
import com.kanevsky.stats.dto.TeamStatsDto;

public interface IStatsService {
    PlayerStatsDto getPlayerStats(String playerName);

    TeamStatsDto getTeamStats(String teamName);
}
