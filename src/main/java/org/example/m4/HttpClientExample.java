package org.example.m4;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpClientExample {
    /**
     * to demonstrate to use http request
     */
    public static void example1() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://www.ibm.com/us-en"))
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        var len = response.body().length();

        System.out.println(len);
    }


    /**
     * to demonstrate to send async http request, and different ways to fetch the result
     * see "--->"
     */
    public static void example2() {
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://www.ibm.com/us-en"))
                .build();

        // ---> use "sendAsync" will return a CompletableFuture
        CompletableFuture<HttpResponse<String>> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        // ---> Method 1:
        // blocking get / join the results
        // or: future.join().body().length();

        // var len = future.get().body().length();
        // System.out.println(len);

        // ---> Method: 2:
        // since thenAccept() by default running in common JF thread
        // the main thread will shut down (also JVM will shut down) before this thread return anything
        //future
        //        .thenAccept(res -> {
        //            var len = res.body().length();
        //            System.out.println(len + " - thenAccept - Running in thread: " + Thread.currentThread().getName());
        //        })
        //        .thenRun(() -> {
        //            System.out.println("Chain other task - thenRun - Running in thread: " + Thread.currentThread().getName());
        //        })
        //        /* ---> blocking the main thread and wait for the result coming back */
        //        .join();

        // ---> Method: 3
        // ---> because you want to free those threads as soon as you get the response
        // by using executor, the rest of the chain will be run in executor's thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        future
                .thenAcceptAsync(res -> {
                    var len = res.body().length();
                    System.out.println(len + " - thenAccept - Running in thread: " + Thread.currentThread().getName());
                }, executorService)
                .thenRun(() -> {
                    System.out.println("Chain other task - thenRun - Running in thread: " + Thread.currentThread().getName());
                })
                /* ---> blocking the main thread and wait for the result coming back */
                .join();

        executorService.shutdown();
    }

    /**
     * to demonstrate to use the dummy CompletableFuture to chain tasks
     * see "--->"
     */
    public static void example3() {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://www.ibm.com/us-en"))
                .build();

        // ---> the goal is to trigger the http request using the dummy
        CompletableFuture<Void> dummy = new CompletableFuture<>();

        // ---> instead of thenApply, we can use thenCompose to flat the CompletableFuture
        // sendAsync -> returns a CompletableFuture
        // thenApply -> returns another CompletableFuture
        //CompletableFuture<CompletableFuture<HttpResponse<String>>> completableFutureCompletableFuture =
        //        dummy.thenApply(nil -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()));

        var future = dummy.thenCompose(nil -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                .thenAcceptAsync(res -> {
                    var len = res.body().length();
                    System.out.println(len + " - thenCompose - Running in thread: " + Thread.currentThread().getName());
                }, executorService)
                .thenRun(() -> {
                    System.out.println("Chain other task - thenRun - Running in thread: " + Thread.currentThread().getName());
                });

        // by running the complete, it allows you to run the tasks in the chain,
        // choose one of the two
        //dummy.complete(null);

        dummy.completeAsync(() -> null, executorService);

        /* ---> blocking the main thread and wait for the result coming back */
        future.join();

        executorService.shutdown();
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        //example1();
        //example2();
        example3();
    }

    private static void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
