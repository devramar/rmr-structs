package dev.ramar.structs;

import java.util.concurrent.CompletableFuture;

import java.util.Optional;

import java.util.Objects;

public final class AsyncField<O, T>
{
    private final O owner;
    private Async<T> value;

    public AsyncField(O owner)
    {
        this.owner = Objects.requireNonNull(owner);
    }

    public void set(T value)
    {
        this.value = Async.Completed(value);
    }

    public void set(CompletableFuture<? extends T> future)
    {
        this.value = Async.Inherit(future);
    }


    public O with(T value)
    {
        this.value = Async.Completed(value);
        return owner;
    }

    public O with(CompletableFuture<? extends T> future)
    {
        this.value = Async.Inherit(future);
        return owner;
    }



    public T get()
    {
        if(value == null)
            throw new IllegalStateException("Async field has not been assigned");

        return value.get();
    }

    public Optional<T> tryGet()
    {
        if( this.isAssigned() )
            return Optional.empty();

        T value = this.value.get();
        if( value == null )
            return Optional.empty();

        return Optional.of(value);
    }


    public Async<T> async()
    {
        return value;
    }



    public boolean isAssigned()
    {
        return value != null;
    }
}