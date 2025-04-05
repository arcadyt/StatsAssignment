package com.kanevsky.stats.repos;

import com.kanevsky.stats.dto.PlayerStatsDto;
import com.kanevsky.stats.dto.StatsBatchRequestDto;
import com.kanevsky.stats.dto.StatsEntryDto;
import com.kanevsky.stats.dto.TeamStatsDto;
import com.kanevsky.stats.model.Stats;
import com.kanevsky.stats.service.IngestService;
import com.kanevsky.stats.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ConcurrentIntegrationTest {

    @Autowired
    private IngestService ingestService;

    @Autowired
    private StatsService statsService;

    @Autowired
    private StatsRepo playerStatsRepo;

    @Autowired
    private StatsRepo teamStatsRepo;

    @Test
    public void testConcurrentIngest() throws InterruptedException {
        // Arrange
        final String playerName = "ConcurrentPlayer";
        final String teamName = "ConcurrentTeam";
        final int numThreads = 10;
        final int gamesPerThread = 5;
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        
        // Act - Multiple threads simultaneously ingesting stats
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < gamesPerThread; j++) {
                        // Each thread submits stats with consistent values for testing
                        StatsEntryDto entry = createStatsEntry(
                                playerName, teamName, 
                                20, 10, 5, 2, 1, 3, 2, 30.0);
                        
                        List<StatsEntryDto> entries = new ArrayList<>();
                        entries.add(entry);
                        
                        ingestService.processBatchEntries(entries);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all threads to complete
        assertTrue(latch.await(20, TimeUnit.SECONDS), "Timed out waiting for ingest threads to complete");
        executor.shutdown();
        
        // Assert - Check that player stats were properly calculated
        PlayerStatsDto playerStats = statsService.getPlayerStats(playerName);
        assertNotNull(playerStats);
        assertEquals(playerName, playerStats.getPlayerName());
        assertEquals(20.0, playerStats.getStats().getPoints());
        assertEquals(10.0, playerStats.getStats().getRebounds());
        assertEquals(5.0, playerStats.getStats().getAssists());
        
        // Team stats should match player stats in this case
        TeamStatsDto teamStats = statsService.getTeamStats(teamName);
        assertNotNull(teamStats);
        assertEquals(teamName, teamStats.getTeamName());
        assertEquals(20.0, teamStats.getStats().getPoints());
        assertEquals(10.0, teamStats.getStats().getRebounds());
        assertEquals(5.0, teamStats.getStats().getAssists());
    }

    @Test
    public void testConcurrentReadsAndWrites() throws InterruptedException {
        // Arrange
        final String playerName = "ReadWritePlayer";
        final String teamName = "ReadWriteTeam";
        final int numWriteThreads = 5;
        final int numReadThreads = 5;
        final int writesPerThread = 10;
        
        ExecutorService executor = Executors.newFixedThreadPool(numWriteThreads + numReadThreads);
        CountDownLatch writeLatch = new CountDownLatch(numWriteThreads);
        CountDownLatch readLatch = new CountDownLatch(numReadThreads);
        
        AtomicReference<Exception> readException = new AtomicReference<>(null);
        
        // Initial stats to ensure the player/team exists
        Stats initialStats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);
        playerStatsRepo.accept(playerName, initialStats);
        teamStatsRepo.accept(teamName, initialStats);
        
        // Act - Submit write tasks
        for (int i = 0; i < numWriteThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < writesPerThread; j++) {
                        StatsEntryDto entry = createStatsEntry(
                                playerName, teamName, 
                                20, 10, 5, 2, 1, 3, 2, 30.0);
                        
                        List<StatsEntryDto> entries = new ArrayList<>();
                        entries.add(entry);
                        
                        ingestService.processBatchEntries(entries);
                        
                        // Small sleep to increase chance of read/write overlap
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    writeLatch.countDown();
                }
            });
        }
        
        // Act - Submit read tasks that run concurrently with the write tasks
        for (int i = 0; i < numReadThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < writesPerThread * 2; j++) {
                        try {
                            PlayerStatsDto playerStats = statsService.getPlayerStats(playerName);
                            TeamStatsDto teamStats = statsService.getTeamStats(teamName);
                            
                            // Simple validation - we just want to ensure no exceptions
                            assertNotNull(playerStats);
                            assertNotNull(teamStats);
                        } catch (Exception e) {
                            readException.set(e);
                            e.printStackTrace();
                        }
                        
                        // Small sleep to increase chance of read/write overlap
                        Thread.sleep(5);
                    }
                } catch (Exception e) {
                    readException.set(e);
                    e.printStackTrace();
                } finally {
                    readLatch.countDown();
                }
            });
        }
        
        // Wait for all threads to complete
        assertTrue(writeLatch.await(30, TimeUnit.SECONDS), "Timed out waiting for write threads to complete");
        assertTrue(readLatch.await(30, TimeUnit.SECONDS), "Timed out waiting for read threads to complete");
        executor.shutdown();
        
        // Assert - No exceptions from readers
        assertNull(readException.get(), "Readers encountered exceptions");
        
        // Final validation of stats
        PlayerStatsDto finalPlayerStats = statsService.getPlayerStats(playerName);
        assertNotNull(finalPlayerStats);
        
        TeamStatsDto finalTeamStats = statsService.getTeamStats(teamName);
        assertNotNull(finalTeamStats);
    }

    @Test
    public void testMultiplePlayersAndTeams() throws InterruptedException {
        // Arrange
        final int numPlayers = 10;
        final int numTeams = 3;
        final int gamesPerPlayer = 5;
        
        ExecutorService executor = Executors.newFixedThreadPool(numPlayers);
        CountDownLatch latch = new CountDownLatch(numPlayers);
        
        // Act - Each player submits their stats concurrently
        for (int i = 0; i < numPlayers; i++) {
            final int playerId = i;
            final String playerName = "Player" + playerId;
            final String teamName = "Team" + (playerId % numTeams); // Distribute players across teams
            
            executor.submit(() -> {
                try {
                    for (int j = 0; j < gamesPerPlayer; j++) {
                        // Vary the stats slightly for each player
                        StatsEntryDto entry = createStatsEntry(
                                playerName, teamName, 
                                20 + playerId, // Different points for each player
                                10, 5, 2, 1, 3, 2, 30.0);
                        
                        List<StatsEntryDto> entries = new ArrayList<>();
                        entries.add(entry);
                        
                        ingestService.processBatchEntries(entries);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all threads to complete
        assertTrue(latch.await(20, TimeUnit.SECONDS), "Timed out waiting for threads to complete");
        executor.shutdown();
        
        // Assert - Check stats for each player
        for (int i = 0; i < numPlayers; i++) {
            String playerName = "Player" + i;
            PlayerStatsDto playerStats = statsService.getPlayerStats(playerName);
            
            assertNotNull(playerStats);
            assertEquals(playerName, playerStats.getPlayerName());
            assertEquals(20 + i, playerStats.getStats().getPoints());
        }
        
        // Check team stats (should be average of all players in team)
        for (int i = 0; i < numTeams; i++) {
            String teamName = "Team" + i;
            TeamStatsDto teamStats = statsService.getTeamStats(teamName);
            
            assertNotNull(teamStats);
            assertEquals(teamName, teamStats.getTeamName());
            
            // We need to calculate the expected average for each team
            // Team0 has players 0, 3, 6, 9 (points: 20, 23, 26, 29)
            // Team1 has players 1, 4, 7 (points: 21, 24, 27)
            // Team2 has players 2, 5, 8 (points: 22, 25, 28)
            
            // Players in each team have indices where (playerId % numTeams) == teamId
            double totalPoints = 0;
            int playerCount = 0;
            
            for (int playerId = 0; playerId < numPlayers; playerId++) {
                if (playerId % numTeams == i) {
                    totalPoints += (20 + playerId);
                    playerCount++;
                }
            }
            
            double expectedAvgPoints = totalPoints / playerCount;
            assertEquals(expectedAvgPoints, teamStats.getStats().getPoints(), 0.001);
        }
    }

    // Helper methods
    
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
    
    private StatsBatchRequestDto createBatchRequest(List<StatsEntryDto> entries) {
        StatsBatchRequestDto request = new StatsBatchRequestDto();
        request.setEntries(entries);
        return request;
    }
}