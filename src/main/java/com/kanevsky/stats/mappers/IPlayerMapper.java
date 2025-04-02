package com.kanevsky.stats.mappers;

import com.kanevsky.stats.dto.PlayerStatsDto;
import com.kanevsky.stats.model.Stats;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IPlayerMapper {
    PlayerStatsDto toPlayerDto(String playerName, Stats stats);
}
