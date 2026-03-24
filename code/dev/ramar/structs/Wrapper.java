package dev.ramar.structs;

public class Wrapper<T>
{
    public Wrapper()
    { this.value = null; }

    public Wrapper(T val)
    { this.value = val; }

    public T value;
}