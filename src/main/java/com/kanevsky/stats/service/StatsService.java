package com.kanevsky.stats.service;

import com.kanevsky.stats.dto.PlayerStatsDto;
import com.kanevsky.stats.dto.TeamStatsDto;
import com.kanevsky.stats.exceptions.ResourceNotFoundException;
import com.kanevsky.stats.mappers.IPlayerMapper;
import com.kanevsky.stats.mappers.ITeamMapper;
import com.kanevsky.stats.model.Stats;
import com.kanevsky.stats.repos.IStatsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class StatsService implements IStatsService {

    @Autowired
    @Qualifier("playerStatsManager")
    private IStatsManager playerStatsManager;

    @Autowired
    @Qualifier("teamStatsManager")
    private IStatsManager teamStatsManager;

    @Autowired
    private IPlayerMapper playerMapper;

    @Autowired
    private ITeamMapper teamMapper;

    @Override
    public PlayerStatsDto getPlayerStats(String playerName) {
        Stats stats = playerStatsManager.getStats(playerName);
        if (stats == null) {
            throw new ResourceNotFoundException("Player not found: " + playerName);
        }
        return playerMapper.toPlayerDto(playerName, stats);
    }

    @Override
    public TeamStatsDto getTeamStats(String teamName) {
        Stats stats = teamStatsManager.getStats(teamName);
        if (stats == null) {
            throw new ResourceNotFoundException("Team not found: " + teamName);
        }
        return teamMapper.toTeamDto(teamName, stats);
    }
}