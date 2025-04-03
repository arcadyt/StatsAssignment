package com.kanevsky.stats.repos;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.kanevsky.stats.model.Stats;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class StatsRepoTest {

    @Test
    void testAcceptFirstStats() {
        StatsRepo repo = new StatsRepo();
        String key = "player1";

        Stats stats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);
        repo.accept(key, stats);

        Stats result = repo.getStats(key);

        assertNotNull(result);
        assertEquals(10, result.getPoints());
        assertEquals(5, result.getRebounds());
        assertEquals(3, result.getAssists());
        assertEquals(1, result.getSteals());
        assertEquals(2, result.getBlocks());
        assertEquals(3, result.getFouls());
        assertEquals(2, result.getTurnovers());
        assertEquals(30, result.getMinutesPlayed());
    }

    @Test
    void testAcceptMultipleStats() {
        StatsRepo repo = new StatsRepo();
        String key = "player1";

        // First game
        Stats game1 = new Stats(10, 5, 3, 1, 2, 3, 2, 30);
        repo.accept(key, game1);

        // Second game
        Stats game2 = new Stats(20, 10, 6, 2, 4, 1, 1, 36);
        repo.accept(key, game2);

        Stats result = repo.getStats(key);

        assertNotNull(result);
        assertEquals(15, result.getPoints());
        assertEquals(7.5, result.getRebounds());
        assertEquals(4.5, result.getAssists());
        assertEquals(1.5, result.getSteals());
        assertEquals(3, result.getBlocks());
        assertEquals(2, result.getFouls());
        assertEquals(1.5, result.getTurnovers());
        assertEquals(33, result.getMinutesPlayed());
    }

    @Test
    void testGetStatsNonExistentKey() {
        StatsRepo repo = new StatsRepo();
        String key = "nonexistent";

        Stats result = repo.getStats(key);

        assertNull(result);
    }

    @Test
    void testMultipleKeys() {
        StatsRepo repo = new StatsRepo();

        // Player 1
        repo.accept("player1", new Stats(10, 5, 3, 1, 2, 3, 2, 30));

        // Player 2
        repo.accept("player2", new Stats(20, 10, 6, 2, 4, 1, 1, 36));

        Stats player1Stats = repo.getStats("player1");
        Stats player2Stats = repo.getStats("player2");

        assertNotNull(player1Stats);
        assertEquals(10, player1Stats.getPoints());

        assertNotNull(player2Stats);
        assertEquals(20, player2Stats.getPoints());
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        final StatsRepo repo = new StatsRepo();
        final String key = "player1";
        final int numThreads = 10;
        final int numUpdatesPerThread = 10;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < numUpdatesPerThread; j++) {
                        repo.accept(key, new Stats(10, 5, 3, 1, 2, 3, 2, 30));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timed out waiting for threads to complete");
        executor.shutdown();

        Stats result = repo.getStats(key);

        assertNotNull(result);
        assertEquals(10, result.getPoints(), 0.000001);
        assertEquals(5, result.getRebounds(), 0.000001);
        assertEquals(3, result.getAssists(), 0.000001);
        assertEquals(1, result.getSteals(), 0.000001);
        assertEquals(2, result.getBlocks(), 0.000001);
        assertEquals(3, result.getFouls(), 0.000001);
        assertEquals(2, result.getTurnovers(), 0.000001);
        assertEquals(30, result.getMinutesPlayed(), 0.000001);
    }

    @Test
    void testConcurrentDifferentKeys() throws InterruptedException {
        final StatsRepo repo = new StatsRepo();
        final int numThreads = 10;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    String key = "player" + index;
                    repo.accept(key, new Stats(index, index, index, index, index, index, index, index));
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timed out waiting for threads to complete");
        executor.shutdown();

        // Verify each player has the correct stats
        for (int i = 0; i < numThreads; i++) {
            String key = "player" + i;
            Stats result = repo.getStats(key);

            assertNotNull(result);
            assertEquals(i, result.getPoints());
            assertEquals(i, result.getRebounds());
        }
    }

    @Test
    void testEmptyKey() {
        StatsRepo repo = new StatsRepo();
        String key = "";

        Stats stats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);
        repo.accept(key, stats);

        Stats result = repo.getStats(key);

        assertNotNull(result);
        assertEquals(10, result.getPoints());
    }

    @Test
    void testNullKey() {
        StatsRepo repo = new StatsRepo();
        String key = null;

        Stats stats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);

        assertThrows(NullPointerException.class, () -> repo.accept(key, stats));
        assertThrows(NullPointerException.class, () -> repo.getStats(key));
    }
}