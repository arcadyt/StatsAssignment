package com.kanevsky.stats.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StatsTest {

    @Test
    void testMultiply() {
        Stats stats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);
        Stats multiplied = stats.multiply(2);

        assertEquals(20, multiplied.getPoints());
        assertEquals(10, multiplied.getRebounds());
        assertEquals(6, multiplied.getAssists());
        assertEquals(2, multiplied.getSteals());
        assertEquals(4, multiplied.getBlocks());
        assertEquals(6, multiplied.getFouls());
        assertEquals(4, multiplied.getTurnovers());
        assertEquals(60, multiplied.getMinutesPlayed());

        assertEquals(10, stats.getPoints());
    }

    @Test
    void testMultiplyByZero() {
        Stats stats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);
        Stats multiplied = stats.multiply(0);

        assertEquals(0, multiplied.getPoints());
        assertEquals(0, multiplied.getRebounds());
        assertEquals(0, multiplied.getAssists());
        assertEquals(0, multiplied.getSteals());
        assertEquals(0, multiplied.getBlocks());
        assertEquals(0, multiplied.getFouls());
        assertEquals(0, multiplied.getTurnovers());
        assertEquals(0, multiplied.getMinutesPlayed());
    }

    @Test
    void testMultiplyByNegative() {
        Stats stats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);
        Stats multiplied = stats.multiply(-1);

        assertEquals(-10, multiplied.getPoints());
        assertEquals(-5, multiplied.getRebounds());
        assertEquals(-3, multiplied.getAssists());
        assertEquals(-1, multiplied.getSteals());
        assertEquals(-2, multiplied.getBlocks());
        assertEquals(-3, multiplied.getFouls());
        assertEquals(-2, multiplied.getTurnovers());
        assertEquals(-30, multiplied.getMinutesPlayed());
    }

    @Test
    void testDivide() {
        Stats stats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);
        Stats divided = stats.divide(2);

        assertEquals(5, divided.getPoints());
        assertEquals(2.5, divided.getRebounds());
        assertEquals(1.5, divided.getAssists());
        assertEquals(0.5, divided.getSteals());
        assertEquals(1, divided.getBlocks());
        assertEquals(1.5, divided.getFouls());
        assertEquals(1, divided.getTurnovers());
        assertEquals(15, divided.getMinutesPlayed());

        // Original should remain unchanged (immutability check)
        assertEquals(10, stats.getPoints());
    }

    @Test
    void testDivideByZero() {
        Stats stats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);

        Stats result = stats.divide(0);

        assertTrue(Double.isInfinite(result.getPoints()));
        assertTrue(Double.isInfinite(result.getRebounds()));
        assertTrue(Double.isInfinite(result.getAssists()));
        assertTrue(Double.isInfinite(result.getSteals()));
        assertTrue(Double.isInfinite(result.getBlocks()));
        assertTrue(Double.isInfinite(result.getFouls()));
        assertTrue(Double.isInfinite(result.getTurnovers()));
        assertTrue(Double.isInfinite(result.getMinutesPlayed()));
    }

    @Test
    void testAdd() {
        Stats stats1 = new Stats(10, 5, 3, 1, 2, 3, 2, 30);
        Stats stats2 = new Stats(20, 10, 7, 3, 1, 2, 4, 40);

        Stats sum = stats1.add(stats2);

        assertEquals(30, sum.getPoints());
        assertEquals(15, sum.getRebounds());
        assertEquals(10, sum.getAssists());
        assertEquals(4, sum.getSteals());
        assertEquals(3, sum.getBlocks());
        assertEquals(5, sum.getFouls());
        assertEquals(6, sum.getTurnovers());
        assertEquals(70, sum.getMinutesPlayed());

        assertEquals(10, stats1.getPoints());
        assertEquals(20, stats2.getPoints());
    }

    @Test
    void testAddNullStats() {
        Stats stats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);

        assertThrows(NullPointerException.class, () -> stats.add(null));
    }

    @Test
    void testChainOperations() {
        Stats stats1 = new Stats(10, 5, 3, 1, 2, 3, 2, 30);
        Stats stats2 = new Stats(20, 10, 7, 3, 1, 2, 4, 40);

        Stats result = stats1.multiply(2).add(stats2).divide(3);

        assertEquals(13.33, result.getPoints(), 0.01);
        assertEquals(6.67, result.getRebounds(), 0.01);
        assertEquals(4.33, result.getAssists(), 0.01);
        assertEquals(1.67, result.getSteals(), 0.01);
        assertEquals(1.67, result.getBlocks(), 0.01);
        assertEquals(2.67, result.getFouls(), 0.01);
        assertEquals(2.67, result.getTurnovers(), 0.01);
        assertEquals(33.33, result.getMinutesPlayed(), 0.01);
    }

    @Test
    void testEqualsAndHashCode() {
        Stats stats1 = new Stats(10, 5, 3, 1, 2, 3, 2, 30);
        Stats stats2 = new Stats(10, 5, 3, 1, 2, 3, 2, 30);
        Stats stats3 = new Stats(20, 10, 7, 3, 1, 2, 4, 40);

        assertEquals(stats1, stats2);
        assertNotEquals(stats1, stats3);

        assertEquals(stats1.hashCode(), stats2.hashCode());
        assertNotEquals(stats1.hashCode(), stats3.hashCode());
    }
}