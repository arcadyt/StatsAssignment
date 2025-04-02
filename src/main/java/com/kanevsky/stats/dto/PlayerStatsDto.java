package com.kanevsky.stats.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.kanevsky.stats.model.Stats;
import lombok.Data;

@Data
public class PlayerStatsDto {
    private String playerName;
    @JsonUnwrapped
    private Stats stats;

}
