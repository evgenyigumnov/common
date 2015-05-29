package com.igumnov.common;

import com.igumnov.common.time.TimeException;
import com.igumnov.common.time.Timer;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Benchmark {

    private static ReentrantReadWriteLock timersLock = new ReentrantReadWriteLock();
    private static HashMap<String, Timer> timers = new HashMap<>();
    protected static final String timerDefaultName = "com.igumnov.common.timer";

    public static void timerStart(String name) throws TimeException {

        Timer timer;
        try {
            timersLock.writeLock().lock();
            if (timers.containsKey(name)) {
                timers.get(name);
                throw new TimeException("timer already started");
            } else {
                timer = new Timer();
                timers.put(name, timer);
            }

            timer.start();

        } finally {
            timersLock.writeLock().unlock();
        }

    }

    public static long timerPause(String name) throws TimeException {
        Timer timer;
        try {
            timersLock.readLock().lock();
            if (timers.containsKey(name)) {
                timer = timers.get(name);
            } else {
                throw new TimeException("startProcedure should be call before");
            }
            return timer.pause();
        } finally {
            timersLock.readLock().unlock();
        }
    }

    public static void timerResume(String name) throws TimeException {
        Timer timer;
        try {
            timersLock.readLock().lock();
            if (timers.containsKey(name)) {
                timer = timers.get(name);
            } else {
                throw new TimeException("startProcedure should be call before");
            }
            timer.resume();

        } finally {
            timersLock.readLock().unlock();
        }

    }

    public static long timerStop(String name) throws TimeException {
        Timer timer;
        try {
            timersLock.writeLock().lock();
            if (timers.containsKey(name)) {
                timer = timers.get(name);
            } else {
                throw new TimeException("startProcedure should be call before");
            }

            long ret = timer.stop();
            timers.remove(name);
            return ret;

        } finally {
            timersLock.writeLock().unlock();
        }
    }

    public static void timerStart() throws TimeException {
        timerStart(Benchmark.timerDefaultName);
    }

    public static long timerStop() throws TimeException {
        return timerStop(Benchmark.timerDefaultName);
    }


    public static long timerPause() throws TimeException {
        return timerPause(Benchmark.timerDefaultName);
    }

    public static void timerResume() throws TimeException {
        timerResume(Benchmark.timerDefaultName);
    }


    public static void timerBegin(String name) throws TimeException {
        Timer timer;
        try {
            timersLock.writeLock().lock();
            if (timers.containsKey(name)) {
                timer = timers.get(name);
                timer.resume();
            } else {
                timer = new Timer();
                timers.put(name, timer);
                timer.start();
            }
        } finally {
            timersLock.writeLock().unlock();
        }

    }

    public static long timerEnd(String name) throws TimeException {
        return timerPause(name);
    }

    public static long timerGetRepeatCount(String name) throws TimeException {

        Timer timer;
        try {
            timersLock.readLock().lock();
            if (timers.containsKey(name)) {
                timer = timers.get(name);
            } else {
                throw new TimeException("startProcedure should be call before");
            }
            return timer.getRepeatCount();

        } finally {
            timersLock.readLock().unlock();
        }
    }

    public static long timerGetAverageTime(String name) throws TimeException {
        Timer timer;
        try {
            timersLock.readLock().lock();
            if (timers.containsKey(name)) {
                timer = timers.get(name);
            } else {
                throw new TimeException("startProcedure should be call before");
            }
            return timer.getAverageTime();

        } finally {
            timersLock.readLock().unlock();
        }
    }

    public static long timerGetTotalTime(String name) throws TimeException {
        Timer timer;
        try {
            timersLock.readLock().lock();
            if (timers.containsKey(name)) {
                timer = timers.get(name);
            } else {
                throw new TimeException("startProcedure should be call before");
            }
            return timer.getTotlaTime();

        } finally {
            timersLock.readLock().unlock();
        }
    }
}
