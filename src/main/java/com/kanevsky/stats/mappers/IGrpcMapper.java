package com.kanevsky.stats.mappers;

import com.kanevsky.stats.dto.StatsEntryDto;
import com.kanevsky.stats.grpc.GameStats;
import com.kanevsky.stats.grpc.StatsEntry;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IGrpcMapper {
    StatsEntryDto toStatsEntryDto(StatsEntry entry);
    
    StatsEntryDto.GameStatsDto toGameStatsDto(GameStats gameStats);
    
    List<StatsEntryDto> toStatsEntryDtoList(List<StatsEntry> entries);
}