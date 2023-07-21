# Async-With-CompletionStage

Course provided 

Asynchronous Programming Using CompletionStage by Jose Paumard

Available at: https://app.pluralsight.com/library/courses/java-fundamentals-asynchronous-programming-completionstage/table-of-contents

## List of Content

### m3

1. **CompletableFutureWithSupplier.example1**: to demonstrate using join to get the CompletableFuture result
2. **CompletableFutureWithSupplier.example2**: to demonstrate using complete() with default value
3. **CompletableFutureWithSupplier.example3**: to demonstrate using obtrudeValue() with default value. You can compare that with the example2
4. **SimpleCompletableFuture.example1**: This example shows a trick to create a completable future that can only be completed by calling its complete() or obtrudeValue()

### m4

1. **AsyncExample.example1**: to demonstrate task running one after another
2. **AsyncExample.example2**: to demonstrate running the consumer in other threads
3. **AsyncExample.example3**: to demonstrate running tasks asynchronously
4. **AsyncExample.example4**: to demonstrate running on different threads
5. **AsyncExampleMultiTasks.example1**: to demonstrate how to wait for both tasks complete, and then run something else
6. **AsyncExampleMultiTasks.example2**: to demonstrate how to continue when either of the tasks finishes first
7. **AsyncException.example1**: to demonstrate using exceptionally()
8. **AsyncException.example2**: to demonstrate using exceptionally() and join
9. **AsyncException.example3**: to demonstrate using whenComplete()
10. **AsyncException.example4**: to demonstrate using handle()
11. **DummyCompletableFutureExample.example1**: to demonstrate using a dummy CompletableFuture<Void> to chain tasks
12. **DummyCompletableFutureExample.example2**: to demonstrate to complete the dummy in an executor thread.
13. **HttpClientExample.example1**: to demonstrate to use http request
14. **HttpClientExample.example2**: to demonstrate to send async http request, and different ways to fetch the result
15. **HttpClientExample.example3**: to demonstrate to use the dummy CompletableFuture to chain tasks