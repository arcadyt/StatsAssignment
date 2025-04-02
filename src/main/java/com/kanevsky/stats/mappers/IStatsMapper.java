package com.kanevsky.stats.mappers;

import com.kanevsky.stats.dto.StatsEntryDto.GameStatsDto;
import com.kanevsky.stats.model.Stats;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IStatsMapper {
    Stats toStats(GameStatsDto gameStatsDto);
}
