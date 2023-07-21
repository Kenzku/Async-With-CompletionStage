package org.example.m4;

import org.example.m4.model.User;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AsyncException {

    /**
     * to demonstrate using exceptionally
     */
    public static void example1() {
        Supplier<List<Long>> supplyIDs = () -> {
            sleep(200);
            System.out.println("Supplier - Running in thread: " + Thread.currentThread().getName());
             throw new IllegalStateException("No data");
            // with the following line, you see no exceptions when run example1()
            //return Arrays.asList(1L, 2L, 3L);
        };

        Function<List<Long>, List<User>> fetchUsers = ids -> {
            sleep(300);
            System.out.println("Function - Running in thread: " + Thread.currentThread().getName());
            return ids.stream().map(User::new).collect(Collectors.toList());
        };


        Consumer<List<User>> logger = users -> {
            System.out.println("Consumer - Running in thread: " + Thread.currentThread().getName());
            users.forEach(System.out::println);
        };

        CompletableFuture<List<Long>> supply = CompletableFuture.supplyAsync(supplyIDs);

        // exceptionally catch the exception from the upstream
        CompletableFuture<List<Long>> exceptionally = supply.exceptionally(e -> {
            System.out.println("Exceptionally - Running in thread: " + Thread.currentThread().getName());
            return List.of();
        });

        CompletableFuture<List<User>> fetch = exceptionally.thenApply(fetchUsers);

        CompletableFuture<Void> logs = fetch.thenAccept(logger);

        // the main thread will finish here before the other threads having chance to show anything
        // so, we sleep here
        sleep(1000);

        System.out.println("Supply : done -> " + supply.isDone() +
                ", Exception -> " + supply.isCompletedExceptionally());

        System.out.println("Fetch : done -> " + fetch.isDone() +
                ", Exception -> " + fetch.isCompletedExceptionally());

        System.out.println("Log : done -> " + logs.isDone() +
                ", Exception -> " + logs.isCompletedExceptionally());
    }

    /**
     * to demonstrate using join
     * see "--->"
     */
    public static void example2() {
        Supplier<List<Long>> supplyIDs = () -> {
            sleep(200);
            System.out.println("Supplier - Running in thread: " + Thread.currentThread().getName());
            throw new IllegalStateException("No data");
            // with the following line, you see no exceptions when run example1()
            //return Arrays.asList(1L, 2L, 3L);
        };

        Function<List<Long>, List<User>> fetchUsers = ids -> {
            sleep(300);
            System.out.println("Function - Running in thread: " + Thread.currentThread().getName());
            return ids.stream().map(User::new).collect(Collectors.toList());
        };


        Consumer<List<User>> logger = users -> {
            System.out.println("Consumer - Running in thread: " + Thread.currentThread().getName());
            users.forEach(System.out::println);
        };

        CompletableFuture<List<Long>> supply = CompletableFuture.supplyAsync(supplyIDs);

        // exceptionally catch the exception from the upstream
        CompletableFuture<List<Long>> exceptionally = supply.exceptionally(e -> {
            System.out.println("Exceptionally - Running in thread: " + Thread.currentThread().getName());
            return List.of();
        });

        CompletableFuture<List<User>> fetch = exceptionally.thenApply(fetchUsers);

        CompletableFuture<Void> logs = fetch.thenAccept(logger);


        // ---> since supply throws an exception, join will also throw an exception
        // java.util.concurrent.CompletionException: java.lang.IllegalStateException
        //supply.join();

        // ---> since supply throws an exception, but handled by exceptionally,
        // join will NOT throw an exception
        // comment out the supply.join(); to use the following line
        exceptionally.join();

        // the main thread will finish here before the other threads having chance to show anything
        // so, we sleep here
        sleep(1000);

        System.out.println("Supply : done -> " + supply.isDone() +
                ", Exception -> " + supply.isCompletedExceptionally());

        System.out.println("Fetch : done -> " + fetch.isDone() +
                ", Exception -> " + fetch.isCompletedExceptionally());

        System.out.println("Log : done -> " + logs.isDone() +
                ", Exception -> " + logs.isCompletedExceptionally());
    }

    /**
     * to demonstrate using whenComplete()
     * the exception will be forwarded to all the downstream CompletableFuture
     * see "--->"
     */
    public static void example3() {
        Supplier<List<Long>> supplyIDs = () -> {
            sleep(200);
            System.out.println("Supplier - Running in thread: " + Thread.currentThread().getName());
            throw new IllegalStateException("No data");
            // with the following line, you see no exceptions when run example1()
            //return Arrays.asList(1L, 2L, 3L);
        };

        Function<List<Long>, List<User>> fetchUsers = ids -> {
            sleep(300);
            System.out.println("Function - Running in thread: " + Thread.currentThread().getName());
            return ids.stream().map(User::new).collect(Collectors.toList());
        };


        Consumer<List<User>> logger = users -> {
            System.out.println("Consumer - Running in thread: " + Thread.currentThread().getName());
            users.forEach(System.out::println);
        };

        CompletableFuture<List<Long>> supply = CompletableFuture.supplyAsync(supplyIDs);

        // catch the exception from the upstream
        // --->
        // the exception will be forwarded to all the downstream CompletableFuture
        // i.e.
        // Supply : done -> true, Exception -> true
        // Fetch : done -> true, Exception -> true
        // Log : done -> true, Exception -> true
        CompletableFuture<List<Long>> whenComplete = supply.whenComplete(
                /* takes BiConsumer (List<Long>, exception)*/
                (ids, exception) -> {
                    System.out.println("whenComplete - Running in thread: " + Thread.currentThread().getName());
                    if (exception != null) {
                        // log: java.lang.IllegalStateException: No data
                        System.out.println(exception.getMessage());
                    } else {
                        // when there is no exception
                    }
                }
        );

        CompletableFuture<List<User>> fetch = whenComplete.thenApply(fetchUsers);

        CompletableFuture<Void> logs = fetch.thenAccept(logger);

        // the main thread will finish here before the other threads having chance to show anything
        // so, we sleep here
        sleep(1000);

        System.out.println("Supply : done -> " + supply.isDone() +
                ", Exception -> " + supply.isCompletedExceptionally());

        System.out.println("Fetch : done -> " + fetch.isDone() +
                ", Exception -> " + fetch.isCompletedExceptionally());

        System.out.println("Log : done -> " + logs.isDone() +
                ", Exception -> " + logs.isCompletedExceptionally());
    }

    /**
     * to demonstrate using handle()
     * the exception will be forwarded to all the downstream CompletableFuture
     * see "--->"
     */
    public static void example4() {
        Supplier<List<Long>> supplyIDs = () -> {
            sleep(200);
            System.out.println("Supplier - Running in thread: " + Thread.currentThread().getName());
            throw new IllegalStateException("No data");
            // with the following line, you see no exceptions when run example1()
            //return Arrays.asList(1L, 2L, 3L);
        };

        Function<List<Long>, List<User>> fetchUsers = ids -> {
            sleep(300);
            System.out.println("Function - Running in thread: " + Thread.currentThread().getName());
            return ids.stream().map(User::new).collect(Collectors.toList());
        };


        Consumer<List<User>> logger = users -> {
            System.out.println("Consumer - Running in thread: " + Thread.currentThread().getName());
            users.forEach(System.out::println);
        };

        CompletableFuture<List<Long>> supply = CompletableFuture.supplyAsync(supplyIDs);

        // catch the exception from the upstream
        // --->
        // the exception will NOT be forwarded to all the downstream CompletableFuture
        // i.e.
        // Supply : done -> true, Exception -> true
        // Fetch : done -> true, Exception -> false
        // Log : done -> true, Exception -> false
        CompletableFuture<List<Long>> handle = supply.handle(
                /* ---> takes BiFunction (List<Long>, exception) -> List<Long> */
                (ids, exception) -> {
                    System.out.println("whenComplete - Running in thread: " + Thread.currentThread().getName());
                    if (exception != null) {
                        // log: java.lang.IllegalStateException: No data
                        System.out.println(exception.getMessage());
                        return List.of();
                    } else {
                        // when there is no exception
                        return ids;
                    }
                }
        );

        CompletableFuture<List<User>> fetch = handle.thenApply(fetchUsers);

        CompletableFuture<Void> logs = fetch.thenAccept(logger);

        // the main thread will finish here before the other threads having chance to show anything
        // so, we sleep here
        sleep(1000);

        System.out.println("Supply : done -> " + supply.isDone() +
                ", Exception -> " + supply.isCompletedExceptionally());

        System.out.println("Fetch : done -> " + fetch.isDone() +
                ", Exception -> " + fetch.isCompletedExceptionally());

        System.out.println("Log : done -> " + logs.isDone() +
                ", Exception -> " + logs.isCompletedExceptionally());
    }

    public static void main(String args[]) {
        //example1();
        //example2();
        //example3();
        example4();
    }

    private static void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
