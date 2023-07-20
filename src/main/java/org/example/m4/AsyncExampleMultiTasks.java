package org.example.m4;

import org.example.m4.model.Email;
import org.example.m4.model.User;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AsyncExampleMultiTasks {

    /**
     * to demonstrate how to wait for both tasks complete, and then run something else
     */
    public static void example1() {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        // pretending a list of user id
        Supplier<List<Long>> supplyIDs = () -> {
            sleep(200);
            System.out.println("Supplier - Running in thread: " + Thread.currentThread().getName());
            return Arrays.asList(1L, 2L, 3L);
        };

        // pretending fetching users
        Function<List<Long>, CompletableFuture<List<User>>> fetchUser = ids -> {
            sleep(300);
            System.out.println("Function - Running in thread: " + Thread.currentThread().getName());

            Supplier<List<User>> userSupplier = () -> {
                System.out.println("Internal User Supplier - Running in thread: " + Thread.currentThread().getName());
                return ids.stream().map(User::new).toList();
            };

            return CompletableFuture.supplyAsync(userSupplier);
        };

        // pretending fetching emails
        Function<List<Long>, CompletableFuture<List<Email>>> fetchEmails = ids -> {
            sleep(500);
            Supplier<List<Email>> emailSupplier = () -> {
                System.out.println("Internal Email Supplier - Running in thread: " + Thread.currentThread().getName());
                return ids.stream().map(Email::new).toList();
            };

            return CompletableFuture.supplyAsync(emailSupplier);
        };

        // the first method is a Supplier
        var cf = CompletableFuture.supplyAsync(supplyIDs);

        // the second and third methods are Functions
        var userCf = cf.thenCompose(fetchUser);
        var emailCf = cf.thenCompose(fetchEmails);

        // will compete (wait) on completion of the userCf and emailCf
        userCf.thenAcceptBoth(emailCf, /*BiConsumer*/(users, emails) -> {
            System.out.println("BiConsumer - Total Users: " + users.size() + "; total emails: " + emails.size());
        });

        // the main thread will finish here before the other threads having chance to show anything
        // so, we sleep here
        sleep(1500);

        // remember to shut down the executor
        executorService.shutdown();
    }

    /**
     * to demonstrate how to continue when either of the tasks finishes first
     * NOTE: you have to compose the fetchUser1 and fetchUser2 in an async way
     * otherwise, you will see the users 2 comes back first, even if it is supposed to take longer to complete
     * see "--->"
     */
    public static void example2() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        // pretending a list of user id
        Supplier<List<Long>> supplyIDs = () -> {
            sleep(200);
            System.out.println("Supplier - Running in thread: " + Thread.currentThread().getName());
            return Arrays.asList(1L, 2L, 3L);
        };

        // pretending fetching users
        Function<List<Long>, CompletableFuture<List<User>>> fetchUser1 = ids -> {
            sleep(150);
            System.out.println("Function - Fetching Users 1 (FASTER) - Running in thread: " + Thread.currentThread().getName());

            Supplier<List<User>> userSupplier = () -> {
                System.out.println("Internal User Supplier 1 (FASTER) - Running in thread: " + Thread.currentThread().getName());
                return ids.stream().map(User::new).toList();
            };

            return CompletableFuture.supplyAsync(userSupplier);
        };

        // pretending fetching users
        Function<List<Long>, CompletableFuture<List<User>>> fetchUser2 = ids -> {
            sleep(300);
            System.out.println("Function - Fetching Users 2 - Running in thread: " + Thread.currentThread().getName());

            Supplier<List<User>> userSupplier = () -> {
                System.out.println("Internal User Supplier 2 - Running in thread: " + Thread.currentThread().getName());
                return ids.stream().map(User::new).toList();
            };

            return CompletableFuture.supplyAsync(userSupplier);
        };

        Consumer<List<User>> logger = users -> {
            System.out.println("Consumer - Running in thread: " + Thread.currentThread().getName());
            users.forEach(System.out::println);
        };

        // the first method is a Supplier
        var cf = CompletableFuture.supplyAsync(supplyIDs);

        // use thenCompose to unwrap the CompletableFuture<List<User>>
        // and get List<User> in the later chains
        // ---> this construction is a non-async way, it implies, users2 comes back first
        // ---> the results depend on which method is submitted and also run first
        //var users1 = cf.thenCompose(fetchUser1);
        //var users2 = cf.thenCompose(fetchUser2);

        // ---> this is the async way
        var users1 = cf.thenComposeAsync(fetchUser1);
        var users2 = cf.thenComposeAsync(fetchUser2);

        users1.thenRun(() -> System.out.println("Runnable - Users 1 (FASTER) - Running in thread: " + Thread.currentThread().getName()));
        users2.thenRun(() -> System.out.println("Runnable - Users 2 - Running in thread: " + Thread.currentThread().getName()));

        // NOTE: users1 and users2 have to be the same type
        users1.acceptEither(users2, logger);

        // the main thread will finish here before the other threads having chance to show anything
        // so, we sleep here
        sleep(1000);

        // remember to shut down the executor
        executorService.shutdown();
    }

    public static void main(String[] args) {
        //example1();
        example2();
    }

    private static void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
        }
    }
}
