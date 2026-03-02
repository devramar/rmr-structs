package dev.ramar.structs;

public final class Pair<A, B>
{
    public final A a;
    public final B b;

    public Pair(A a, B b)
    {
        this.a = a;
        this.b = b;
    }

    public static <A, B> Pair<A, B> Of(A a, B b)
    { return new Pair<>(a, b); }
}