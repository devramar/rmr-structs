package dev.ramar.structs;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Async<T>
{
    /* Construction
    --===-------------
    */

    public static <T> Async<T> Completed(T value)
    {
        Async<T> exp = new Async();
        exp.set(value);
        return exp;
    }
    public static <T> Async<T> Manual()
    {
        return new Async<>(new CompletableFuture<>());
    }

    public static <T> Async<T> Inherit(CompletableFuture<? extends T> future)
    {
        return new Async<>(future.thenApply(x -> x));
    }
    

    private final CompletableFuture<T> future;
    public CompletableFuture<T> future()
    { return future; }

    private Async()
    {
        this(new CompletableFuture<>());
    }
    private Async(CompletableFuture<T> future)
    {
        this.future = Objects.requireNonNull(future);
    }


    /* Access
    --===-------
    */

    /// silently blocks until future completes
    public T get()
    {
        return future.join();
    }


    /// throws if it's not ready now
    public T requireReady()
    {
        if( !future.isDone() )
            throw new IllegalStateException("Async value is not ready");

        return future.join();
    }


    /// Optional.empty() if it's not ready now
    public Optional<T> tryGet()
    {
        if( !isReady() )
            return Optional.empty();

        return Optional.ofNullable(future.join());
    }



    /* State Info
    --===-----------
    */

    public boolean isDone()
    { return future.isDone(); }


    public boolean isReady()
    {
        return future.isDone()
            && !future.isCompletedExceptionally()
            && !future.isCancelled();
    }

    public boolean isFailed()
    { return future.isCompletedExceptionally(); }

    public boolean isCancelled()
    { return future.isCancelled(); }


    /* Manual Completion
    --===------------------
    */

    /// complete the internal future with <value>
    public void set(T value)
    {
        boolean success = future.complete(value);

        if( !success )
            throw new IllegalStateException("Async value has already been completed");
    }

    /// fail the internal future with <ex>
    public void fail(Throwable ex)
    {
        Objects.requireNonNull(ex);

        boolean success = future.completeExceptionally(ex);

        if( !success )
            throw new IllegalStateException("Async value has already been completed");
    }

    
}