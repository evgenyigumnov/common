package com.igumnov.common;


import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class Task {

    private static ScheduledExecutorService executorSheduler = Executors.newScheduledThreadPool(10);
    private static int shedulerPoolSize = 10;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private static int poolSize = 10;

    public static Future<?> startProcedure(Runnable procedure) {
        Future<?> future = executor.submit(procedure);
        return future;
    }


    public static Future<Object> startFunction(Callable<Object> function) {
        Future<Object> future = executor.submit(function);
        return future;
    }

    public static void setThreadPoolSize(int size) {
        if (poolSize != size) {
            executor.shutdown();
            executor = Executors.newFixedThreadPool(size);
            poolSize = size;
        }
    }

    public static void setShedulerPoolSize(int size) {
        if (poolSize != size) {
            executorSheduler.shutdown();
            executorSheduler = Executors.newScheduledThreadPool(size);
            shedulerPoolSize = size;
        }
    }


    public static void schedule(Runnable procedure, double repeatAfterSeconds) {
        executorSheduler.schedule(() -> {
            try {
                procedure.run();
            } finally {
                Task.schedule(procedure, repeatAfterSeconds);
            }
        }, (long) (repeatAfterSeconds * 1000), TimeUnit.MILLISECONDS);
    }

    public static void timerRepeated(Runnable procedure, Calendar when, TimeUnit repeatEvery) {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                procedure.run();
            }
        }, when.getTime(), TimeUnit.MILLISECONDS.convert(1, repeatEvery));

    }
}
