package com.kanevsky.stats.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatsEntryDto {
    @NotBlank(message = "Player name cannot be empty")
    private String playerName;
    
    @NotBlank(message = "Team name cannot be empty")
    private String teamName;

    @Valid
    @NotNull(message = "Stats cannot be null")
    @JsonUnwrapped
    private GameStatsDto stats;
    
    @Data
    public static class GameStatsDto {
        @Min(value = 0, message = "Points cannot be negative")
        private int points;
        
        @Min(value = 0, message = "Rebounds cannot be negative")
        private int rebounds;
        
        @Min(value = 0, message = "Assists cannot be negative")
        private int assists;
        
        @Min(value = 0, message = "Steals cannot be negative")
        private int steals;
        
        @Min(value = 0, message = "Blocks cannot be negative")
        private int blocks;
        
        @Min(value = 0, message = "Fouls cannot be negative")
        @Max(value = 6, message = "Fouls cannot exceed 6")
        private int fouls;
        
        @Min(value = 0, message = "Turnovers cannot be negative")
        private int turnovers;
        
        @Min(value = 0, message = "Minutes played cannot be negative")
        @Max(value = 48, message = "Minutes played cannot exceed 48")
        private double minutesPlayed;
    }
}