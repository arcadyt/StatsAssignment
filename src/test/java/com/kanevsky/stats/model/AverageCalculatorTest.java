package com.kanevsky.stats.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class AverageCalculatorTest {

    @Test
    void testInitialState() {
        AverageCalculator calculator = new AverageCalculator();
        Stats average = calculator.getAverage();

        assertEquals(0, average.getPoints());
        assertEquals(0, average.getRebounds());
        assertEquals(0, average.getAssists());
        assertEquals(0, average.getSteals());
        assertEquals(0, average.getBlocks());
        assertEquals(0, average.getFouls());
        assertEquals(0, average.getTurnovers());
        assertEquals(0, average.getMinutesPlayed());
    }

    @Test
    void testAddFirstGameStats() {
        AverageCalculator calculator = new AverageCalculator();

        Stats gameStats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);
        calculator.accept(gameStats);

        Stats average = calculator.getAverage();

        assertEquals(10, average.getPoints());
        assertEquals(5, average.getRebounds());
        assertEquals(3, average.getAssists());
        assertEquals(1, average.getSteals());
        assertEquals(2, average.getBlocks());
        assertEquals(3, average.getFouls());
        assertEquals(2, average.getTurnovers());
        assertEquals(30, average.getMinutesPlayed());
    }

    @Test
    void testAddMultipleGameStats() {
        AverageCalculator calculator = new AverageCalculator();

        // Game 1
        Stats game1 = new Stats(20, 10, 5, 2, 1, 3, 2, 36);
        calculator.accept(game1);

        // Game 2
        Stats game2 = new Stats(10, 5, 3, 1, 0, 2, 1, 24);
        calculator.accept(game2);

        Stats average = calculator.getAverage();

        assertEquals(15, average.getPoints());
        assertEquals(7.5, average.getRebounds());
        assertEquals(4, average.getAssists());
        assertEquals(1.5, average.getSteals());
        assertEquals(0.5, average.getBlocks());
        assertEquals(2.5, average.getFouls());
        assertEquals(1.5, average.getTurnovers());
        assertEquals(30, average.getMinutesPlayed());
    }

    @Test
    void testExtremeValues() {
        AverageCalculator calculator = new AverageCalculator();

        // First game with extreme high values
        Stats game1 = new Stats(100, 50, 30, 15, 10, 6, 20, 48);
        calculator.accept(game1);

        // Second game with all zeros
        Stats game2 = new Stats(0, 0, 0, 0, 0, 0, 0, 0);
        calculator.accept(game2);

        Stats average = calculator.getAverage();

        assertEquals(50, average.getPoints());
        assertEquals(25, average.getRebounds());
        assertEquals(15, average.getAssists());
        assertEquals(7.5, average.getSteals());
        assertEquals(5, average.getBlocks());
        assertEquals(3, average.getFouls());
        assertEquals(10, average.getTurnovers());
        assertEquals(24, average.getMinutesPlayed());
    }

    @Test
    void testAverageRecalculationFormula() {
        AverageCalculator calculator = new AverageCalculator();

        // Add 3 games with different stats to verify the recalculation formula works correctly
        Stats game1 = new Stats(30, 15, 10, 5, 2, 4, 3, 40);
        Stats game2 = new Stats(20, 10, 5, 2, 1, 2, 2, 30);
        Stats game3 = new Stats(10, 5, 0, 0, 0, 0, 1, 20);

        calculator.accept(game1);
        calculator.accept(game2);
        calculator.accept(game3);

        Stats average = calculator.getAverage();

        assertEquals(20, average.getPoints());
        assertEquals(10, average.getRebounds());
        assertEquals(5, average.getAssists());
        assertEquals(2.3333333333333335, average.getSteals(), 0.0001);
        assertEquals(1, average.getBlocks());
        assertEquals(2, average.getFouls());
        assertEquals(2, average.getTurnovers());
        assertEquals(30, average.getMinutesPlayed());
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        final int numThreads = 10;
        final int numGamesPerThread = 10;
        final AverageCalculator calculator = new AverageCalculator();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < numGamesPerThread; j++) {
                        Stats gameStats = new Stats(10, 5, 3, 1, 2, 3, 2, 30);
                        calculator.accept(gameStats);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timed out waiting for threads to complete");
        executor.shutdown();

        Stats average = calculator.getAverage();

        assertEquals(10, average.getPoints(), 0.00001);
        assertEquals(5, average.getRebounds(), 0.00001);
        assertEquals(3, average.getAssists(), 0.00001);
        assertEquals(1, average.getSteals(), 0.00001);
        assertEquals(2, average.getBlocks(), 0.00001);
        assertEquals(3, average.getFouls(), 0.00001);
        assertEquals(2, average.getTurnovers(), 0.00001);
        assertEquals(30, average.getMinutesPlayed(), 0.00001);
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        final int numThreads = 5;
        final AverageCalculator calculator = new AverageCalculator();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        executor.submit(() -> {
            try {
                calculator.accept(new Stats(10, 0, 0, 0, 0, 0, 0, 10));
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                calculator.accept(new Stats(20, 0, 0, 0, 0, 0, 0, 20));
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                calculator.accept(new Stats(30, 0, 0, 0, 0, 0, 0, 30));
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                calculator.accept(new Stats(40, 0, 0, 0, 0, 0, 0, 40));
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                calculator.accept(new Stats(50, 0, 0, 0, 0, 0, 0, 50));
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timed out waiting for threads to complete");
        executor.shutdown();

        Stats average = calculator.getAverage();

        // Expected average: (10 + 20 + 30 + 40 + 50) / 5 = 30 points
        assertEquals(30, average.getPoints());
        assertEquals(30, average.getMinutesPlayed());
    }

    @Test
    void testReadDuringWrite() throws InterruptedException {
        final AverageCalculator calculator = new AverageCalculator();
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(1);

        calculator.accept(new Stats(10, 5, 3, 1, 2, 3, 2, 30));

        Thread writerThread = new Thread(() -> {
            try {
                startLatch.await();

                Thread.sleep(500);

                calculator.accept(new Stats(20, 10, 6, 2, 4, 1, 1, 40));

                endLatch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        writerThread.start();

        Thread readerThread = new Thread(() -> {
            try {
                startLatch.countDown();

                Thread.sleep(100);

                Stats average = calculator.getAverage();

                assertEquals(10, average.getPoints());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        readerThread.start();

        assertTrue(endLatch.await(5, TimeUnit.SECONDS), "Timed out waiting for operations to complete");
        readerThread.join();
        writerThread.join();

        Stats finalAverage = calculator.getAverage();
        assertEquals(15, finalAverage.getPoints());
    }
}