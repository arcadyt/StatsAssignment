package com.kanevsky.stats.model;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AverageCalculator {

   private int gamesPlayed = 0;
   private Stats curAvg = new Stats();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void accept(Stats gameStats){
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            curAvg = curAvg.multiply(gamesPlayed).add(gameStats).divide(++gamesPlayed);

        }
        finally {
            writeLock.unlock();
        }

    }

    public Stats getAverage() {
        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            return curAvg;
        } finally {
            readLock.unlock();
        }
    }
}
