package org.example.m4;

import org.example.m4.model.User;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AsyncExample {

    /**
     * to demonstrate task running one after another
     */
    public static void example1() {
        // pretending a list of user id
        Supplier<List<Long>> supplyIDs = () -> {
            sleep(200);
            return Arrays.asList(1L, 2L, 3L);
        };

        // pretending fetching users from DB
        Function<List<Long>, List<User>> fetchUser = ids -> {
            sleep(300);
            return ids.stream().map(User::new).collect(Collectors.toList());
        };

        Consumer<List<User>> logger = users -> users.forEach(System.out::println);

        // the first method is a Supplier
        var cf = CompletableFuture.supplyAsync(supplyIDs);
        // the second method is a Function
        cf.thenApply(fetchUser)
                .thenAccept(logger);

        // the main thread will finish here before the other threads having chance to show anything
        // so, we sleep here
        sleep(1000);
    }

    /**
     * to demonstrate running the consumer in other threads
     */
    public static void example2() {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        // pretending a list of user id
        Supplier<List<Long>> supplyIDs = () -> {
            sleep(200);
            System.out.println("Supplier - Running in thread: " + Thread.currentThread().getName());
            return Arrays.asList(1L, 2L, 3L);
        };

        // pretending fetching users from DB
        Function<List<Long>, List<User>> fetchUser = ids -> {
            sleep(300);
            System.out.println("Function - Running in thread: " + Thread.currentThread().getName());
            return ids.stream().map(User::new).collect(Collectors.toList());
        };

        Consumer<List<User>> logger = users -> {
            System.out.println("Consumer - Running in thread: " + Thread.currentThread().getName());
            users.forEach(System.out::println);
        };

        // the first method is a Supplier
        var cf = CompletableFuture.supplyAsync(supplyIDs);
        // the second method is a Function
        cf.thenApply(fetchUser)
                .thenAcceptAsync(logger, executorService);

        // the main thread will finish here before the other threads having chance to show anything
        // so, we sleep here
        sleep(1000);

        // remember to shut down the executor
        executorService.shutdown();
    }

    /**
     * to demonstrate running tasks asynchronously
     * see " ---> "
     */
    public static void example3() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        // pretending a list of user id
        Supplier<List<Long>> supplyIDs = () -> {
            sleep(200);
            System.out.println("Supplier - Running in thread: " + Thread.currentThread().getName());
            return Arrays.asList(1L, 2L, 3L);
        };

        // pretending fetching users from DB
        // ---> instead of returning List<User> you wrap it in CompletableFuture
        Function<List<Long>, CompletableFuture<List<User>>> fetchUser = ids -> {
            sleep(300);
            System.out.println("Function - Running in thread: " + Thread.currentThread().getName());

            // ---> since you are returning a CompletableFuture<List<User>>, you need to wrap the return value
            Supplier<List<User>> userSupplier = () -> {

                // ---> this will run in a separate thread
                System.out.println("Internal Supplier - Running in thread: " + Thread.currentThread().getName());

                return ids.stream().map(User::new).collect(Collectors.toList());
            };

            // ---> note: nothing is getting executed now, until the supplier's get method is called.
            // this "get" is called by the internal supplyAsync API
            return CompletableFuture.supplyAsync(userSupplier);
        };

        Consumer<List<User>> logger = users -> {
            System.out.println("Consumer - Running in thread: " + Thread.currentThread().getName());
            users.forEach(System.out::println);
        };

        // the first method is a Supplier
        var cf = CompletableFuture.supplyAsync(supplyIDs);

        // the second method is a Function
        // ---> but now we have to unwrap the completable future
        cf.thenCompose(fetchUser)
                .thenAcceptAsync(logger, executorService);

        // the main thread will finish here before the other threads having chance to show anything
        // so, we sleep here
        sleep(1000);

        // remember to shut down the executor
        executorService.shutdown();
    }

    /**
     * to demonstrate running on different threads
     * see " ---> "
     */
    public static void example4() {
        ExecutorService executorService1 = Executors.newSingleThreadExecutor();
        ExecutorService executorService2 = Executors.newSingleThreadExecutor();

        Supplier<List<Long>> supplyIDs = () -> {
            sleep(200);
            System.out.println("Supplier - Running in thread: " + Thread.currentThread().getName());
            return Arrays.asList(1L, 2L, 3L);
        };

        // pretending fetching users from DB
        // instead of returning List<User> you wrap it in CompletableFuture
        Function<List<Long>, CompletableFuture<List<User>>> fetchUser = ids -> {
            sleep(300);
            System.out.println("Function - Running in thread: " + Thread.currentThread().getName());

            // since you are returning a CompletableFuture<List<User>>, you need to wrap the return value
            Supplier<List<User>> userSupplier = () -> {

                // this will run in a separate thread
                System.out.println("Internal Supplier - Running in thread: " + Thread.currentThread().getName());

                return ids.stream().map(User::new).collect(Collectors.toList());
            };

            // note: nothing is getting executed now, until the supplier's get method is called.
            // this "get" is called by the internal supplyAsync API
            // ---> use supplyAsync to make internal supplier running in executorService1
            return CompletableFuture.supplyAsync(userSupplier, executorService1);
        };

        Consumer<List<User>> logger = users -> {
            System.out.println("Consumer - Running in thread: " + Thread.currentThread().getName());
            users.forEach(System.out::println);
        };

        // the first method is a Supplier
        var cf = CompletableFuture.supplyAsync(supplyIDs);

        // the second method is a Function
        // but now we have to unwrap the completable future
        // ---> use thenComposeAsync to make fetchUser running in executorService1
        // NOTE: it does not automatically change the running thread of the internal supplier
        cf.thenComposeAsync(fetchUser, executorService1)
                .thenAcceptAsync(logger, executorService2);

        // the main thread will finish here before the other threads having chance to show anything
        // so, we sleep here
        sleep(1000);
        executorService1.shutdown();
        executorService2.shutdown();
    }

    public static void main(String[] args) {
        //example1();
        //example2();
        //example3();
        example4();
    }

    private static void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
        }
    }
}
