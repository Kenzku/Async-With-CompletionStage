package org.example.m4;

import org.example.m4.model.Email;
import org.example.m4.model.User;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class AsyncExampleMultiTasks {

    record NewObject(String s1, String s2) {}

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

    /**
     * to demonstrate a CompletableFuture completes when ANY of the tasks completes
     */
    public static void example3() {
        Supplier<String> s1 = () -> {
            sleep(200);
            System.out.println("s1");
            return "s1";
        };

        Supplier<String> s2 = () -> {
            sleep(150);
            System.out.println("s2");
            return "s2";
        };

        Supplier<String> s3 = () -> {
            sleep(100);
            System.out.println("s3");
            return "s3";
        };

        // these three supplyAsync will run in the background, regardless having anyOf() or not.
        var cf1 = CompletableFuture.supplyAsync(s1);
        var cf2 = CompletableFuture.supplyAsync(s2);
        var cf3 = CompletableFuture.supplyAsync(s3);

        // anyOf takes CompletableFutures of the same type
        var cf = CompletableFuture.anyOf(cf1, cf2, cf3);

        // cf will return s3, as s3 is the fastest
        cf.thenAccept(string -> {
            System.out.println("cf completes: " + string);
        }).join();

        // you shall only see cf3 is completed
        System.out.println("cf1 completed = " + cf1.isDone());
        System.out.println("cf2 completed = " + cf2.isDone());
        System.out.println("cf3 completed = " + cf3.isDone());

        System.out.println("Without join(), this line will run first, and the main thread will shutdown");
    }

    /**
     * to demonstrate a CompletableFuture completes when ALL the tasks completes
     * and how to consume their results
     * see "--->"
     */
    public static void example4() {
        Supplier<Integer> s1 = () -> {
            sleep(200);
            System.out.println("s1 - 200");
            return 200;
        };

        Supplier<Integer> s2 = () -> {
            sleep(150);
            System.out.println("s2 - 150");
            return 150;
        };

        Supplier<Integer> s3 = () -> {
            sleep(100);
            System.out.println("s3 - 100");
            return 100;
        };

        // these three supplyAsync will run in the background, regardless having anyOf() or not.
        var cf1 = CompletableFuture.supplyAsync(s1);
        var cf2 = CompletableFuture.supplyAsync(s2);
        var cf3 = CompletableFuture.supplyAsync(s3);

        // ---> "allOf" carries CompletableFuture of any types as its parameters, but it does not contain results.
        // ---> this is why we use <Void>
        CompletableFuture<Void> allDone = CompletableFuture.allOf(cf1, cf2, cf3);

        // ---> e.g. if we want to get the minimum value of all the three CompletableFutures
        // 1. use Stream API, e.g. Stream.of, passing in the CompletableFutures cf1, cf2, cf3
        int smallestNumber = allDone.thenApply(
                /* ---> the cf1, cf2, cf3 have completed already, so they will not re-run again here - they simply contain results */
                nil /*null*/ -> Stream.of(cf1, cf2, cf3)
                        /* ---> convert the CompletableFuture to their results, by using join() */
                        .map(cf -> {
                            System.out.println("Getting results - " + Thread.currentThread().getName());
                            /* ---> we use the join to get the results -
                            in this case, the join only runs when all of them completes,
                            so it will NOT block the processing thread. */
                            return cf.join();
                        })
                        /*Function.identity() return the value itself for comparison*/
                        .min(Comparator.comparing(Function.identity()))
                        .orElseThrow()
        // you need the last join() here to hold the main thread, until the allDone CF completes.
        ).join();

        // this code is blocked
        System.out.println("Min result - " + smallestNumber + " - " + Thread.currentThread().getName());
    }

    /**
     * to demonstrate when to use thenCompose
     * Imagine you need to create new object, based on the results from two long-running tasks
     * This pattern is used when you want to combine two completable futures that carries two asynchronous results.
     */
    public static void example5() {
        Supplier<String> s1 = () -> {
            sleep(200);
            System.out.println("s1");
            return "s1";
        };

        Supplier<String> s2 = () -> {
            sleep(150);
            System.out.println("s2");
            return "s2";
        };

        var cf1 = CompletableFuture.supplyAsync(s1);
        var cf2 = CompletableFuture.supplyAsync(s2);

        // Method 1:
        // you can use the get() / join()
        // this also blocks the main thread
        // var newObject = new NewObject(cf1.join(), cf2.join());
        //System.out.println("New Object: " + newObject);

        // Method 2:
        // you can use thenCombine()
        // use 1 future (cf1) to combine the second (cf2)
        //var newObject = cf1
        //        .thenCombine(cf2, (r1 /*result of s1*/, r2/*result of s2*/) -> new NewObject(r1, r2))
        //        .join();
        //System.out.println("New Object: " + newObject);

        // Method 3:
        // you can use thenCompose()
        // use 1 future (cf1) to compose a CompletableFuture by using:
        // the result of cf1 and the second CompletableFuture (cf2)
        // i.e. you return a CompletableFuture inside the thenCompose() function, then get flatten (like flatMap)
        // This is the major difference, e.g. inside the supplyAsync(s1) you return a String
        cf1.thenCompose(
                r1 /*result of cf1*/ -> cf2.thenApply(r2 /*result of cf2*/ -> new NewObject(r1, r2))
        ).thenAccept(
                newObject -> System.out.println("New Object: " + newObject)
        ).join();
    }
    public static void main(String[] args) {
        //example1();
        //example2();
        //example3();
        //example4();
        example5();

    }

    private static void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
        }
    }
}
