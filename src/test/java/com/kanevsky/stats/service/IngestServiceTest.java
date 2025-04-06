package com.kanevsky.stats.service;

import com.kanevsky.stats.dto.StatsEntryDto;
import com.kanevsky.stats.mappers.IStatsMapper;
import com.kanevsky.stats.model.Stats;
import com.kanevsky.stats.repos.IStatsManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IngestServiceTest {

    @Mock
    private IStatsMapper statsMapper;

    @Mock
    private IStatsManager playerStatsManager;

    @Mock
    private IStatsManager teamStatsManager;

    @InjectMocks
    private IngestService ingestService;

    private StatsEntryDto validStatsEntry;
    private Stats mappedStats;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup valid stats entry
        validStatsEntry = new StatsEntryDto();
        validStatsEntry.setPlayerName("John Doe");
        validStatsEntry.setTeamName("Lakers");

        StatsEntryDto.GameStatsDto gameStats = new StatsEntryDto.GameStatsDto();
        gameStats.setPoints(20);
        gameStats.setRebounds(5);
        gameStats.setAssists(3);
        gameStats.setSteals(1);
        gameStats.setBlocks(0);
        gameStats.setFouls(2);
        gameStats.setTurnovers(1);
        gameStats.setMinutesPlayed(25.5);

        validStatsEntry.setStats(gameStats);

        // Setup mapped stats
        mappedStats = new Stats(20, 5, 3, 1, 0, 2, 1, 25.5);

        // Mock the mapper
        when(statsMapper.toStats(validStatsEntry.getStats())).thenReturn(mappedStats);
    }

    @Test
    void testProcessStatsEntry_Success() {
        // Setup
        when(playerStatsManager.submit(mappedStats, "John Doe")).thenReturn(true);
        when(teamStatsManager.submit(mappedStats, "Lakers")).thenReturn(true);

        // Execute
        boolean result = ingestService.processStatsEntry(validStatsEntry);

        // Verify
        assertTrue(result);
        verify(statsMapper).toStats(validStatsEntry.getStats());
        verify(playerStatsManager).submit(mappedStats, "John Doe");
        verify(teamStatsManager).submit(mappedStats, "Lakers");
    }

    @Test
    void testProcessStatsEntry_PlayerSubmitFailure() {
        // Setup
        when(playerStatsManager.submit(mappedStats, "John Doe")).thenReturn(false);
        when(teamStatsManager.submit(mappedStats, "Lakers")).thenReturn(true);

        // Execute
        boolean result = ingestService.processStatsEntry(validStatsEntry);

        // Verify
        assertFalse(result);
        verify(statsMapper).toStats(validStatsEntry.getStats());
        verify(playerStatsManager).submit(mappedStats, "John Doe");
        verify(teamStatsManager).submit(mappedStats, "Lakers");
    }

    @Test
    void testProcessStatsEntry_TeamSubmitFailure() {
        // Setup
        when(playerStatsManager.submit(mappedStats, "John Doe")).thenReturn(true);
        when(teamStatsManager.submit(mappedStats, "Lakers")).thenReturn(false);

        // Execute
        boolean result = ingestService.processStatsEntry(validStatsEntry);

        // Verify
        assertFalse(result);
        verify(statsMapper).toStats(validStatsEntry.getStats());
        verify(playerStatsManager).submit(mappedStats, "John Doe");
        verify(teamStatsManager).submit(mappedStats, "Lakers");
    }

    @Test
    void testProcessStatsEntry_BothSubmitFailure() {
        // Setup
        when(playerStatsManager.submit(mappedStats, "John Doe")).thenReturn(false);
        when(teamStatsManager.submit(mappedStats, "Lakers")).thenReturn(false);

        // Execute
        boolean result = ingestService.processStatsEntry(validStatsEntry);

        // Verify
        assertFalse(result);
        verify(statsMapper).toStats(validStatsEntry.getStats());
        verify(playerStatsManager).submit(mappedStats, "John Doe");
        verify(teamStatsManager).submit(mappedStats, "Lakers");
    }

    @Test
    void testProcessStatsEntry_MappingException() {
        // Setup
        when(statsMapper.toStats(validStatsEntry.getStats())).thenThrow(new RuntimeException("Mapping error"));

        // Execute
        boolean result = ingestService.processStatsEntry(validStatsEntry);

        // Verify
        assertFalse(result);
        verify(statsMapper).toStats(validStatsEntry.getStats());
        verifyNoInteractions(playerStatsManager);
        verifyNoInteractions(teamStatsManager);
    }

    @Test
    void testProcessBatchEntries_EmptyList() {
        // Execute
        int result = ingestService.processBatchEntries(Collections.emptyList());

        // Verify
        assertEquals(0, result);
        verifyNoInteractions(statsMapper);
        verifyNoInteractions(playerStatsManager);
        verifyNoInteractions(teamStatsManager);
    }

    @Test
    void testProcessBatchEntries_NullList() {
        // Execute
        int result = ingestService.processBatchEntries(null);

        // Verify
        assertEquals(0, result);
        verifyNoInteractions(statsMapper);
        verifyNoInteractions(playerStatsManager);
        verifyNoInteractions(teamStatsManager);
    }

    @Test
    void testProcessBatchEntries_AllSuccess() {
        // Setup
        StatsEntryDto entry1 = validStatsEntry;

        StatsEntryDto entry2 = new StatsEntryDto();
        entry2.setPlayerName("Jane Smith");
        entry2.setTeamName("Celtics");
        entry2.setStats(validStatsEntry.getStats());

        List<StatsEntryDto> entries = Arrays.asList(entry1, entry2);

        when(playerStatsManager.submit(any(Stats.class), anyString())).thenReturn(true);
        when(teamStatsManager.submit(any(Stats.class), anyString())).thenReturn(true);
        when(statsMapper.toStats(any())).thenReturn(mappedStats);

        // Execute
        int result = ingestService.processBatchEntries(entries);

        // Verify
        assertEquals(2, result);
        verify(statsMapper, times(2)).toStats(any());
        verify(playerStatsManager).submit(mappedStats, "John Doe");
        verify(playerStatsManager).submit(mappedStats, "Jane Smith");
        verify(teamStatsManager).submit(mappedStats, "Lakers");
        verify(teamStatsManager).submit(mappedStats, "Celtics");
    }

    @Test
    void testProcessBatchEntries_MixedSuccess() {
        // Setup
        StatsEntryDto entry1 = validStatsEntry;

        StatsEntryDto entry2 = new StatsEntryDto();
        entry2.setPlayerName("Jane Smith");
        entry2.setTeamName("Celtics");
        entry2.setStats(validStatsEntry.getStats());

        List<StatsEntryDto> entries = Arrays.asList(entry1, entry2);

        when(statsMapper.toStats(any())).thenReturn(mappedStats);

        // First entry succeeds for both player and team
        when(playerStatsManager.submit(mappedStats, "John Doe")).thenReturn(true);
        when(teamStatsManager.submit(mappedStats, "Lakers")).thenReturn(true);

        // Second entry fails for player
        when(playerStatsManager.submit(mappedStats, "Jane Smith")).thenReturn(false);
        when(teamStatsManager.submit(mappedStats, "Celtics")).thenReturn(true);

        // Execute
        int result = ingestService.processBatchEntries(entries);

        // Verify
        assertEquals(1, result);
        verify(statsMapper, times(2)).toStats(any());
    }

    @Test
    void testProcessBatchEntries_AllFail() {
        // Setup
        StatsEntryDto entry1 = validStatsEntry;

        StatsEntryDto entry2 = new StatsEntryDto();
        entry2.setPlayerName("Jane Smith");
        entry2.setTeamName("Celtics");
        entry2.setStats(validStatsEntry.getStats());

        List<StatsEntryDto> entries = Arrays.asList(entry1, entry2);

        when(statsMapper.toStats(any())).thenReturn(mappedStats);
        when(playerStatsManager.submit(any(Stats.class), anyString())).thenReturn(false);
        when(teamStatsManager.submit(any(Stats.class), anyString())).thenReturn(false);

        // Execute
        int result = ingestService.processBatchEntries(entries);

        // Verify
        assertEquals(0, result);
        verify(statsMapper, times(2)).toStats(any());
    }

    @Test
    void testProcessBatchEntries_ExceptionInOneEntry() {
        // Setup
        StatsEntryDto entry1 = new StatsEntryDto();
        entry1.setPlayerName("John Doe");
        entry1.setTeamName("Lakers");

        // Create a new GameStatsDto for entry1
        StatsEntryDto.GameStatsDto gameStats1 = new StatsEntryDto.GameStatsDto();
        gameStats1.setPoints(20);
        gameStats1.setRebounds(5);
        gameStats1.setAssists(3);
        gameStats1.setSteals(1);
        gameStats1.setBlocks(0);
        gameStats1.setFouls(2);
        gameStats1.setTurnovers(1);
        gameStats1.setMinutesPlayed(25.5);
        entry1.setStats(gameStats1);

        // Create a completely separate entry2 with its own GameStatsDto
        StatsEntryDto entry2 = new StatsEntryDto();
        entry2.setPlayerName("Jane Smith");
        entry2.setTeamName("Celtics");

        // Create a different GameStatsDto for entry2
        StatsEntryDto.GameStatsDto gameStats2 = new StatsEntryDto.GameStatsDto();
        gameStats2.setPoints(15);
        gameStats2.setRebounds(8);
        gameStats2.setAssists(6);
        gameStats2.setSteals(2);
        gameStats2.setBlocks(1);
        gameStats2.setFouls(3);
        gameStats2.setTurnovers(2);
        gameStats2.setMinutesPlayed(30.0);
        entry2.setStats(gameStats2);

        List<StatsEntryDto> entries = Arrays.asList(entry1, entry2);

        // Set up the first call to return mappedStats
        when(statsMapper.toStats(gameStats1)).thenReturn(mappedStats);

        // Set up the second call to throw an exception
        when(statsMapper.toStats(gameStats2)).thenThrow(new RuntimeException("Mapping error"));

        // Mock the player and team manager to return true
        when(playerStatsManager.submit(mappedStats, "John Doe")).thenReturn(true);
        when(teamStatsManager.submit(mappedStats, "Lakers")).thenReturn(true);

        // Execute
        int result = ingestService.processBatchEntries(entries);

        // Verify
        assertEquals(1, result);
        verify(statsMapper, times(2)).toStats(any());
        verify(playerStatsManager, times(1)).submit(any(Stats.class), anyString());
        verify(teamStatsManager, times(1)).submit(any(Stats.class), anyString());
    }
}