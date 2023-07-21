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

public class DummyCompletableFutureExample {

    /**
     * To demonstrate using a dummy CompletableFuture<Void> to chain tasks
     */
    public static void example1() {
        Supplier<List<Long>> supplyIDs = () -> {
            sleep(200);
            System.out.println("Supplier - Running in thread: " + Thread.currentThread().getName());
            return Arrays.asList(1L, 2L, 3L);
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

        // this dummy will never complete,
        // unless we call the dummy.complete(null) or dummy.completeAsync(() -> null, executor)
        // because the Void type, you can only provide null into the complete() method
        CompletableFuture<Void> dummy = new CompletableFuture<>();

        // the difference
        // before: supply = CompletableFuture.supplyAsync(supplyIDs);
        // the supply is completed by the task inside the function supplyAsync()
        // for dummy, you need to manually complete it
        CompletableFuture<List<Long>> supply = dummy.thenApply(nil /**/ -> supplyIDs.get());

        CompletableFuture<List<User>> fetch = supply.thenApply(fetchUsers);

        CompletableFuture<Void> logs = fetch.thenAccept(logger);

        // without this line, none of the tasks: supply / fetch / logs will be triggered
        dummy.complete(null);

        // the main thread will finish here before the other threads having chance to show anything
        // so, we sleep here
        sleep(1000);

    }

    /**
     * to demonstrate to complete the dummy in an executor thread.
     * see "--->"
     */
    public static void example2() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Supplier<List<Long>> supplyIDs = () -> {
            sleep(200);
            System.out.println("Supplier - Running in thread: " + Thread.currentThread().getName());
            return Arrays.asList(1L, 2L, 3L);
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

        // this dummy will never complete,
        // unless we call the dummy.complete(null) or dummy.completeAsync(() -> null, executor)
        // because the Void type, you can only provide null into the complete() method
        CompletableFuture<Void> dummy = new CompletableFuture<>();

        // the difference
        // before: supply = CompletableFuture.supplyAsync(supplyIDs);
        // the supply is completed by the task inside the function supplyAsync()
        // for dummy, you need to manually complete it
        CompletableFuture<List<Long>> supply = dummy.thenApply(nil /**/ -> supplyIDs.get());

        CompletableFuture<List<User>> fetch = supply.thenApply(fetchUsers);

        CompletableFuture<Void> logs = fetch.thenAccept(logger);

        // without this line, none of the tasks: supply / fetch / logs will be triggered
        // ---> this will cause error, because the first parameter in completeAsync is a supplier
        // by using null, the source code "completeAsync" throws NullPointerException
        // dummy.completeAsync(null, executorService);

        dummy.completeAsync(() -> null, executorService);

        // the main thread will finish here before the other threads having chance to show anything
        // so, we sleep here
        sleep(1000);

        // shutdown executor
        executorService.shutdown();
    }

    public static void main(String args[]) {
        //example1();
        example2();
    }

    private static void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
