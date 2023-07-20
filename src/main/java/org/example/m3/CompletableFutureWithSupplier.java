package org.example.m3;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class CompletableFutureWithSupplier {


    public static void example1() {
        Supplier<String> supplier = () -> Thread.currentThread().getName();

        var future = CompletableFuture.supplyAsync(supplier);

        // because the main thread will wait at join(), so we do not need: Thread.sleep(1000);
        String result = future.join();

        System.out.println("Result: " + result);
    }

    public static void example2() {
        Supplier<String> supplier = () -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return Thread.currentThread().getName();
        };

        var future = CompletableFuture.supplyAsync(supplier);

        // if the task is not done, overwrite with default value
        future.complete("default value");

        String result = future.join();

        System.out.println("Result 1: " + result);
    }

    public static void example3() {
        Supplier<String> supplier = () -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return Thread.currentThread().getName();
        };

        var future = CompletableFuture.supplyAsync(supplier);


        String result = future.join();
        System.out.println("Result 1: " + result);

        // if the task is already done, it ALSO overwrite with default value
        future.obtrudeValue("default value");

        result = future.join();
        System.out.println("Result 2: " + result);

    }

    public static void main(String[] args) throws InterruptedException {
        //example1();
        //example2();
        example3();
    }
}
