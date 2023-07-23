package org.example.m5;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class SpecifyingThreadExample {
    /**
     * to demonstrate how to specify threads
     */
    public static void example1() {

        ExecutorService executorService1 = Executors.newFixedThreadPool(1);
        ExecutorService executorService3 = Executors.newFixedThreadPool(1);


        Supplier<String> s1 = () -> {
            sleep(200);
            System.out.println("s1 - Running in " + Thread.currentThread().getName());
            return "s1";
        };

        Supplier<String> s2 = () -> {
            sleep(150);
            System.out.println("s2 - Running in " + Thread.currentThread().getName());
            return "s2";
        };

        Supplier<String> s3 = () -> {
            sleep(100);
            System.out.println("s3 - Running in " + Thread.currentThread().getName());
            return "s3";
        };

        // convention 1: if the method ends with Async,
        // it is likely it has a method override allowing you passing an Executor,
        // then the task will run in the thread provided by the Executor.
        // cf1 will run in something like pool-1-thread-1
        var cf1 = CompletableFuture.supplyAsync(s1, executorService1);

        // convention 2: the downstream tasks is executed in the same threads as their parent
        var cf2 = CompletableFuture
                // running on ForkJoinPool.commonPool-worker-1
                .supplyAsync(s2).thenApply(r2 -> {
            // also ForkJoinPool.commonPool-worker-1
            System.out.println("s2 completes"  + " - Running in " + Thread.currentThread().getName());
            return r2;
        });

        var cf3 = CompletableFuture
                .supplyAsync(s3)
                // like convention 1, you can switch threads in the downstream tasks by using the `Async` version
                // and provide an Executor.
                // Using the Async version without an Executor does NOT guarantee the task runs in a different thread.
                .thenApplyAsync(r3 -> {
                    System.out.println("s3 completes"  + " - Running in " + Thread.currentThread().getName());
                    return r3;
                }, executorService3);

        // anyOf takes CompletableFutures of the same type
        var cf = CompletableFuture.anyOf(cf1, cf2, cf3);

        // cf will return s3, as s3 is the fastest
        // cf will run on the thread the same as the one which completes fastest
        cf.thenAccept(string -> {
            System.out.println("cf completes: " + string + " - Running in " + Thread.currentThread().getName());
        }).join();

        // remember to shut down
        executorService1.shutdown();
        executorService3.shutdown();
    }

    public static void main(String[] args) {
        example1();
    }

    private static void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
        }
    }
}
