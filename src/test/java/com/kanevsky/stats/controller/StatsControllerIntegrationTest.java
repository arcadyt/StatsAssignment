package com.kanevsky.stats.controller;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanevsky.stats.dto.StatsBatchRequestDto;
import com.kanevsky.stats.dto.StatsEntryDto;
import com.kanevsky.stats.dto.PlayerStatsDto;
import com.kanevsky.stats.dto.TeamStatsDto;
import com.kanevsky.stats.service.IngestService;
import com.kanevsky.stats.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StatsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IngestService ingestService;

    @Autowired
    private StatsService statsService;

    @Test
    public void testIngestAndRetrievePlayerStats() throws Exception {
        // Create test data
        String playerName = "LeBron James";
        String teamName = "Lakers";

        StatsBatchRequestDto requestDto = createTestBatchRequest(playerName, teamName);

        // Ingest stats
        mockMvc.perform(post("/api/ingest/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        // Retrieve player stats
        MvcResult result = mockMvc.perform(get("/api/stats/player/{playerName}", playerName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify response
        PlayerStatsDto playerStats = objectMapper.readValue(
                result.getResponse().getContentAsString(), PlayerStatsDto.class);

        assertEquals(playerName, playerStats.getPlayerName());
        assertEquals(25.0, playerStats.getStats().getPoints());
        assertEquals(10.0, playerStats.getStats().getRebounds());
        assertEquals(8.0, playerStats.getStats().getAssists());
    }

    @Test
    public void testIngestAndRetrieveTeamStats() throws Exception {
        // Create test data
        String playerName1 = "Kevin Durant";
        String playerName2 = "Kyrie Irving";
        String teamName = "Nets";

        StatsBatchRequestDto requestDto = new StatsBatchRequestDto();
        List<StatsEntryDto> entries = new ArrayList<>();

        // First player stats
        entries.add(createStatsEntry(playerName1, teamName, 30, 7, 5, 1, 1, 2, 3, 35.5));

        // Second player stats
        entries.add(createStatsEntry(playerName2, teamName, 28, 4, 10, 2, 0, 3, 2, 34.0));

        requestDto.setEntries(entries);

        // Ingest stats
        mockMvc.perform(post("/api/ingest/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        // Retrieve team stats
        MvcResult result = mockMvc.perform(get("/api/stats/team/{teamName}", teamName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify response
        TeamStatsDto teamStats = objectMapper.readValue(
                result.getResponse().getContentAsString(), TeamStatsDto.class);

        assertEquals(teamName, teamStats.getTeamName());
        assertEquals(29.0, teamStats.getStats().getPoints());
        assertEquals(5.5, teamStats.getStats().getRebounds());
        assertEquals(7.5, teamStats.getStats().getAssists());
    }

    @Test
    public void testIngestInvalidStats() throws Exception {
        // Create invalid test data (negative points)
        StatsBatchRequestDto requestDto = new StatsBatchRequestDto();
        List<StatsEntryDto> entries = new ArrayList<>();

        StatsEntryDto invalidEntry = new StatsEntryDto();
        invalidEntry.setPlayerName("James Harden");
        invalidEntry.setTeamName("Rockets");

        StatsEntryDto.GameStatsDto invalidStats = new StatsEntryDto.GameStatsDto();
        invalidStats.setPoints(-5); // Invalid: negative points
        invalidStats.setRebounds(5);
        invalidStats.setAssists(7);
        invalidStats.setSteals(1);
        invalidStats.setBlocks(0);
        invalidStats.setFouls(2);
        invalidStats.setTurnovers(3);
        invalidStats.setMinutesPlayed(36.0);

        invalidEntry.setStats(invalidStats);
        entries.add(invalidEntry);
        requestDto.setEntries(entries);

        // Expect validation error
        mockMvc.perform(post("/api/ingest/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRetrieveNonExistentPlayerStats() throws Exception {
        String nonExistentPlayer = "NonExistentPlayer";

        mockMvc.perform(get("/api/stats/player/{playerName}", nonExistentPlayer)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRetrieveNonExistentTeamStats() throws Exception {
        String nonExistentTeam = "NonExistentTeam";

        mockMvc.perform(get("/api/stats/team/{teamName}", nonExistentTeam)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testMultipleIngests() throws Exception {
        // Create test data
        String playerName = "Stephen Curry";
        String teamName = "Warriors";

        // First game stats
        StatsBatchRequestDto game1Request = createStatsRequestForPlayer(playerName, teamName,
                30, 5, 6, 2, 0, 2, 3, 36.0);

        // Second game stats
        StatsBatchRequestDto game2Request = createStatsRequestForPlayer(playerName, teamName,
                35, 6, 8, 3, 1, 1, 2, 38.0);

        // Ingest first game
        mockMvc.perform(post("/api/ingest/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(game1Request)))
                .andExpect(status().isOk());

        // Ingest second game
        mockMvc.perform(post("/api/ingest/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(game2Request)))
                .andExpect(status().isOk());

        // Retrieve player stats to see the average
        MvcResult result = mockMvc.perform(get("/api/stats/player/{playerName}", playerName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        PlayerStatsDto playerStats = objectMapper.readValue(
                result.getResponse().getContentAsString(), PlayerStatsDto.class);

        // Verify averages
        assertEquals(playerName, playerStats.getPlayerName());
        assertEquals(32.5, playerStats.getStats().getPoints()); // (30 + 35) / 2
        assertEquals(5.5, playerStats.getStats().getRebounds()); // (5 + 6) / 2
        assertEquals(7.0, playerStats.getStats().getAssists()); // (6 + 8) / 2
    }

    // Helper methods

    private StatsBatchRequestDto createTestBatchRequest(String playerName, String teamName) {
        StatsBatchRequestDto requestDto = new StatsBatchRequestDto();
        List<StatsEntryDto> entries = new ArrayList<>();

        StatsEntryDto entry = createStatsEntry(playerName, teamName, 25, 10, 8, 2, 1, 2, 3, 38.0);
        entries.add(entry);

        requestDto.setEntries(entries);
        return requestDto;
    }

    private StatsEntryDto createStatsEntry(
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

    private StatsBatchRequestDto createStatsRequestForPlayer(
            String playerName, String teamName,
            int points, int rebounds, int assists,
            int steals, int blocks, int fouls,
            int turnovers, double minutesPlayed) {

        StatsBatchRequestDto requestDto = new StatsBatchRequestDto();
        List<StatsEntryDto> entries = new ArrayList<>();

        entries.add(createStatsEntry(playerName, teamName, points, rebounds, assists,
                steals, blocks, fouls, turnovers, minutesPlayed));

        requestDto.setEntries(entries);
        return requestDto;
    }
}