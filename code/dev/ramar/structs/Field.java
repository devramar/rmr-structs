package dev.ramar.structs;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class Field<O, T extends Object>
{
    private static final Object NOTHING = new Object();

    public final O owner;
    private T value = (T)NOTHING;

    public Field(O owner)
    {
        this.owner = Objects.requireNonNull(owner);
    }


    public Field(O owner, T value)
    {
        this(owner);
        this.value = value;
    }

    public void set(T value)
    {
        this.value = value;
    }

    public O with(T value)
    {
        this.value = value;
        return owner;
    }


    public boolean isEmpty()
    { return ((Object)this.value) == NOTHING; }


    public Optional<T> tryGet()
    {
        return this.isEmpty()
            ? Optional.empty()
            : Optional.of(this.value)
        ;
    }

    public T get()
    {
        if( this.isEmpty() )
            throw new IllegalStateException("Async field has not been assigned");

        return value;
    }

}