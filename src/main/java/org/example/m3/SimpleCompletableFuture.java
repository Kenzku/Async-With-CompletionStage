package org.example.m3;

import java.util.concurrent.CompletableFuture;

public class SimpleCompletableFuture {

    /**
     * This example shows a trick to create a completable future
     * that can only be completed by calling its complete() or obtrudeValue()
     */
    public static void example1() {
        var cf = new CompletableFuture<>();

        Runnable task = () -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            cf.complete(null);
        };

        CompletableFuture.runAsync(task);

        cf.join();
        System.out.println("We are done");
    }

    public static void main(String[] args) {
        example1();
    }
}
