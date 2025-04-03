package com.kanevsky.stats.repos;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.kanevsky.stats.model.Stats;

class StatsManagerTest {

    @Mock
    private StatsRepo statsRepo;

    @InjectMocks
    private StatsManager statsManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSubmitSuccess() {
        String name = "testPlayer";
        Stats stats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);

        doNothing().when(statsRepo).accept(anyString(), any(Stats.class));

        boolean result = statsManager.submit(stats, name);

        assertTrue(result);
        verify(statsRepo).accept(name, stats);
    }

    @Test
    void testSubmitFailure() {
        String name = "testPlayer";
        Stats stats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);

        doThrow(new RuntimeException("Test exception")).when(statsRepo).accept(anyString(), any(Stats.class));

        boolean result = statsManager.submit(stats, name);

        assertFalse(result);
        verify(statsRepo).accept(name, stats);
    }

    @Test
    void testGetStats() {
        String name = "testPlayer";
        Stats expectedStats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);

        when(statsRepo.getStats(name)).thenReturn(expectedStats);

        Stats result = statsManager.getStats(name);

        assertSame(expectedStats, result);
        verify(statsRepo).getStats(name);
    }

    @Test
    void testGetStatsNonExistent() {
        String name = "nonExistentPlayer";

        when(statsRepo.getStats(name)).thenReturn(null);

        Stats result = statsManager.getStats(name);

        assertNull(result);
        verify(statsRepo).getStats(name);
    }

    @Test
    void testIntegration() {
        // Create real objects for integration test
        StatsRepo realRepo = new StatsRepo();
        StatsManager realManager = new StatsManager(realRepo);

        String name = "testPlayer";
        Stats stats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);

        boolean submitResult = realManager.submit(stats, name);
        Stats getResult = realManager.getStats(name);

        assertTrue(submitResult);
        assertNotNull(getResult);
        assertEquals(10, getResult.getPoints());
    }

    @Test
    void testNullName() {
        Stats stats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);

        doThrow(new NullPointerException()).when(statsRepo).accept(isNull(), any(Stats.class));

        boolean result = statsManager.submit(stats, null);

        assertFalse(result);
    }

    @Test
    void testNullStats() {
        String name = "testPlayer";

        boolean result = statsManager.submit(null, name);

        assertFalse(result);
        verifyNoInteractions(statsRepo);
    }
}