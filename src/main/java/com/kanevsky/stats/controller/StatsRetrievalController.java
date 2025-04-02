package com.kanevsky.stats.controller;

import com.kanevsky.stats.dto.PlayerStatsDto;
import com.kanevsky.stats.dto.TeamStatsDto;
import com.kanevsky.stats.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsRetrievalController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/player/{playerName}")
    public ResponseEntity<PlayerStatsDto> getPlayerStats(@PathVariable String playerName) {
        PlayerStatsDto playerStatsDto = statsService.getPlayerStats(playerName);
        
        if (playerStatsDto == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(playerStatsDto);
    }

    @GetMapping("/team/{teamName}")
    public ResponseEntity<TeamStatsDto> getTeamStats(@PathVariable String teamName) {
        TeamStatsDto teamStatsDto = statsService.getTeamStats(teamName);
        
        if (teamStatsDto == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(teamStatsDto);
    }
}