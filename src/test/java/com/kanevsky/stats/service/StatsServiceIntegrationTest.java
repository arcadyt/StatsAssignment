package com.kanevsky.stats.service;

import com.kanevsky.stats.dto.PlayerStatsDto;
import com.kanevsky.stats.dto.StatsEntryDto;
import com.kanevsky.stats.dto.TeamStatsDto;
import com.kanevsky.stats.exceptions.ResourceNotFoundException;
import com.kanevsky.stats.mappers.IPlayerMapper;
import com.kanevsky.stats.mappers.IStatsMapper;
import com.kanevsky.stats.mappers.ITeamMapper;
import com.kanevsky.stats.model.Stats;
import com.kanevsky.stats.repos.IStatsManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class StatsServiceIntegrationTest {

    @MockBean
    @Qualifier("playerStatsManager")
    private IStatsManager playerStatsManager;

    @MockBean
    @Qualifier("teamStatsManager")
    private IStatsManager teamStatsManager;

    @Autowired
    private StatsService statsService;

    @Autowired
    private IngestService ingestService;

    @Autowired
    private IStatsMapper statsMapper;

    @Autowired
    private IPlayerMapper playerMapper;

    @Autowired
    private ITeamMapper teamMapper;

    @Test
    public void testGetPlayerStats() {
        // Arrange
        String playerName = "Giannis Antetokounmpo";
        Stats mockStats = new Stats(29.5, 11.2, 5.8, 1.3, 1.5, 3.1, 3.2, 34.1);

        when(playerStatsManager.getStats(playerName)).thenReturn(mockStats);

        // Act
        PlayerStatsDto result = statsService.getPlayerStats(playerName);

        // Assert
        assertNotNull(result);
        assertEquals(playerName, result.getPlayerName());
        assertEquals(mockStats.getPoints(), result.getStats().getPoints());
        assertEquals(mockStats.getRebounds(), result.getStats().getRebounds());
        assertEquals(mockStats.getAssists(), result.getStats().getAssists());

        verify(playerStatsManager).getStats(playerName);
    }

    @Test
    public void testGetPlayerStatsNotFound() {
        // Arrange
        String playerName = "NonExistentPlayer";

        when(playerStatsManager.getStats(playerName)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            statsService.getPlayerStats(playerName);
        });

        verify(playerStatsManager).getStats(playerName);
    }

    @Test
    public void testGetTeamStats() {
        // Arrange
        String teamName = "Bucks";
        Stats mockStats = new Stats(118.2, 48.3, 25.9, 7.5, 5.2, 19.1, 13.2, 240.0);

        when(teamStatsManager.getStats(teamName)).thenReturn(mockStats);

        // Act
        TeamStatsDto result = statsService.getTeamStats(teamName);

        // Assert
        assertNotNull(result);
        assertEquals(teamName, result.getTeamName());
        assertEquals(mockStats.getPoints(), result.getStats().getPoints());
        assertEquals(mockStats.getRebounds(), result.getStats().getRebounds());
        assertEquals(mockStats.getAssists(), result.getStats().getAssists());

        verify(teamStatsManager).getStats(teamName);
    }

    @Test
    public void testGetTeamStatsNotFound() {
        // Arrange
        String teamName = "NonExistentTeam";

        when(teamStatsManager.getStats(teamName)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            statsService.getTeamStats(teamName);
        });

        verify(teamStatsManager).getStats(teamName);
    }

    @Test
    public void testProcessStatsEntry() {
        // Arrange
        StatsEntryDto entryDto = createStatsEntryDto("Damian Lillard", "Trail Blazers",
                32, 4, 9, 1, 0, 2, 3, 36.0);

        Stats mappedStats = statsMapper.toStats(entryDto.getStats());

        when(playerStatsManager.submit(any(Stats.class), eq(entryDto.getPlayerName()))).thenReturn(true);
        when(teamStatsManager.submit(any(Stats.class), eq(entryDto.getTeamName()))).thenReturn(true);

        // Act
        boolean result = ingestService.processStatsEntry(entryDto);

        // Assert
        assertTrue(result);
        verify(playerStatsManager).submit(any(Stats.class), eq(entryDto.getPlayerName()));
        verify(teamStatsManager).submit(any(Stats.class), eq(entryDto.getTeamName()));
    }

    @Test
    public void testProcessStatsEntryFailure() {
        // Arrange
        StatsEntryDto entryDto = createStatsEntryDto("Damian Lillard", "Trail Blazers",
                32, 4, 9, 1, 0, 2, 3, 36.0);

        when(playerStatsManager.submit(any(Stats.class), eq(entryDto.getPlayerName()))).thenReturn(false);
        when(teamStatsManager.submit(any(Stats.class), eq(entryDto.getTeamName()))).thenReturn(true);

        // Act
        boolean result = ingestService.processStatsEntry(entryDto);

        // Assert
        assertFalse(result);
        verify(playerStatsManager).submit(any(Stats.class), eq(entryDto.getPlayerName()));
        verify(teamStatsManager).submit(any(Stats.class), eq(entryDto.getTeamName()));
    }

    @Test
    public void testProcessBatchEntries() {
        // Arrange
        List<StatsEntryDto> entries = new ArrayList<>();

        // Add three entries
        entries.add(createStatsEntryDto("Player1", "Team1", 10, 5, 3, 1, 0, 2, 1, 20.0));
        entries.add(createStatsEntryDto("Player2", "Team1", 15, 7, 2, 0, 1, 3, 2, 25.0));
        entries.add(createStatsEntryDto("Player3", "Team2", 20, 8, 5, 2, 2, 1, 3, 30.0));

        // Mock first two succeed, third one fails
        when(playerStatsManager.submit(any(Stats.class), eq("Player1"))).thenReturn(true);
        when(teamStatsManager.submit(any(Stats.class), eq("Team1"))).thenReturn(true);

        when(playerStatsManager.submit(any(Stats.class), eq("Player2"))).thenReturn(true);
        when(teamStatsManager.submit(any(Stats.class), eq("Team1"))).thenReturn(true);

        when(playerStatsManager.submit(any(Stats.class), eq("Player3"))).thenReturn(false);
        when(teamStatsManager.submit(any(Stats.class), eq("Team2"))).thenReturn(true);

        // Act
        int successCount = ingestService.processBatchEntries(entries);

        // Assert
        assertEquals(2, successCount);

        verify(playerStatsManager, times(3)).submit(any(Stats.class), anyString());
        verify(teamStatsManager, times(3)).submit(any(Stats.class), anyString());
    }

    @Test
    public void testProcessEmptyBatch() {
        // Arrange
        List<StatsEntryDto> emptyList = new ArrayList<>();

        // Act
        int result = ingestService.processBatchEntries(emptyList);

        // Assert
        assertEquals(0, result);
        verifyNoInteractions(playerStatsManager, teamStatsManager);
    }

    // Helper methods

    private StatsEntryDto createStatsEntryDto(
            String playerName, String teamName,
            int points, int rebounds, int assists,
            int steals, int blocks, int fouls,
            int turnovers, double minutesPlayed) {

        StatsEntryDto entry = new StatsEntryDto();
        entry.setPlayerName(playerName);
        entry.setTeamName(teamName);

        StatsEntryDto.GameStatsDto stats = new StatsEntryDto.GameStatsDto();
        stats.setPoints(points);
        stats.setRebounds(rebounds);
        stats.setAssists(assists);
        stats.setSteals(steals);
        stats.setBlocks(blocks);
        stats.setFouls(fouls);
        stats.setTurnovers(turnovers);
        stats.setMinutesPlayed(minutesPlayed);

        entry.setStats(stats);
        return entry;
    }
}