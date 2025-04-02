package com.kanevsky.stats.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class StatsBatchRequestDto {
    @Valid
    @NotEmpty(message = "At least one stats entry is required")
    private List<StatsEntryDto> entries;
}