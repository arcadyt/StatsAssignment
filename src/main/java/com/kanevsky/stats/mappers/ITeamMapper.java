package com.kanevsky.stats.mappers;

import com.kanevsky.stats.dto.TeamStatsDto;
import com.kanevsky.stats.model.Stats;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ITeamMapper {
    TeamStatsDto toTeamDto(String teamName, Stats stats);
}
