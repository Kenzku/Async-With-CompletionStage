package org.example.m3;

import java.util.concurrent.CompletableFuture;

public class FirstCompletableFuture {
    public static void main(String[] args) throws InterruptedException {
        CompletableFuture.runAsync(() -> System.out.println("You need to thread.sleep(100) to see me"));

        // because the main thread dies before the other thread can print out
        Thread.sleep(1000);
    }
}
