package com.kanevsky.stats.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.kanevsky.stats.model.Stats;
import lombok.Data;

@Data
public class TeamStatsDto {
    private String teamName;
    @JsonUnwrapped
    private Stats stats;
}
