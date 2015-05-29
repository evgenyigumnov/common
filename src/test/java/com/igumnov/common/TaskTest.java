package com.igumnov.common;

import com.igumnov.common.time.TimeException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TaskTest {

    @Test
    public void testStartProcedure() throws InterruptedException, ExecutionException, TimeException {

        Benchmark.timerBegin("TaskStart");
        Benchmark.timerBegin("TaskProgress");
        Future<?> task = Task.startProcedure(() -> Time.sleepInSeconds(2));
        assertTrue(Benchmark.timerStop("TaskStart") < 0.5 * 1000);
        task.get();
        assertTrue(Benchmark.timerStop("TaskProgress") > 1 * 1000);

    }

    @Test
    public void testStartFunction() throws InterruptedException, ExecutionException, TimeException {
        Benchmark.timerBegin("TaskStart2");
        Benchmark.timerBegin("TaskProgress2");
        Future<Object> ret = Task.startFunction(() -> {
            Time.sleepInSeconds(2);
            return "123";
        });

        assertTrue(Benchmark.timerStop("TaskStart2") < 0.5 * 1000);
        assertEquals("123", ret.get());
        assertTrue(Benchmark.timerStop("TaskProgress2") > 1 * 1000);

    }

}