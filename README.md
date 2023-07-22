# Async-With-CompletionStage

Courses Available at:

Asynchronous Programming Using CompletionStage by Jose Paumard

https://app.pluralsight.com/library/courses/java-fundamentals-asynchronous-programming-completionstage/table-of-contents

and 

https://app.pluralsight.com/library/courses/java-se-17-asynchronous-programming/table-of-contents

I also provided my independent opinions via comments.

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
7. **AsyncExampleMultiTasks.example3**: to demonstrate a CompletableFuture completes when any of the tasks completes
8. **AsyncExampleMultiTasks.example4**: to demonstrate a CompletableFuture completes when ALL the tasks completes, and how to consume their results
9. **AsyncExampleMultiTasks.example5**: to demonstrate when to use thenCompose
10. **AsyncException.example1**: to demonstrate using exceptionally()
11. **AsyncException.example2**: to demonstrate using exceptionally() and join
12. **AsyncException.example3**: to demonstrate using whenComplete()
13. **AsyncException.example4**: to demonstrate using handle()
14. **DummyCompletableFutureExample.example1**: to demonstrate using a dummy CompletableFuture<Void> to chain tasks
15. **DummyCompletableFutureExample.example2**: to demonstrate to complete the dummy in an executor thread.
16. **HttpClientExample.example1**: to demonstrate to use http request
17. **HttpClientExample.example2**: to demonstrate to send async http request, and different ways to fetch the result
18. **HttpClientExample.example3**: to demonstrate to use the dummy CompletableFuture to chain tasks