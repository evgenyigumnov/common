package com.igumnov.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Task {

    public static ProcedureThread startProcedure(ProcedureInterface instructions) {
        ProcedureThread thread = new ProcedureThread(instructions);
        thread.start();
        return thread;
    }


    public static Future<Object> startFunction(FunctionInterface function) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Object> future = executor.submit(new TaskCallable(function));
        return future;
    }
}
