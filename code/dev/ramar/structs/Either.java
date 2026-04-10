package dev.ramar.structs;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import java.util.concurrent.Callable;

/// GPT-Made
/**
 * A simple disjoint-union type representing one of two possible values.
 *
 * An Either<L, R> is:
 *  - Left(L)  → typically used for error / alternative path
 *  - Right(R) → typically used for success / primary value
 *
 * Only one side is ever populated.
 *
 * Useful when you want to:
 *  - Return a value OR an error without throwing
 *  - Model dual-type outcomes explicitly
 *  - Compose logic functionally via map / flatMap
 */
public final class Either<L, R>
{
    private final L left;
    private final R right;
    private final boolean isLeft;

    private Either(L left, R right, boolean isLeft)
    {
        this.left = left;
        this.right = right;
        this.isLeft = isLeft;
    }


    /**
     * Functional interface allowing checked exceptions.
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T, E extends Exception>
    {
        T get() throws E;
    }

    /**
     * Executes the supplier and captures any thrown exception
     * as Either.Left(exception).
     *
     * The exception type is preserved.
     */
    public static <T> Either<Exception, T> Try(Callable<T> supplier)
    {
        Objects.requireNonNull(supplier, "supplier");

        try
        {
            return Either.Right(supplier.call());
        }
        catch (Exception ex)
        {
            return Either.Left(ex);
        }
    }

    public static <T, E extends Exception> Either<E, T> TryChecked(ThrowingSupplier<T, E> supplier)
    {
        Objects.requireNonNull(supplier, "supplier");

        try
        {
            return Either.Right(supplier.get());
        }
        catch (Exception ex)
        {
            @SuppressWarnings("unchecked")
            E typed = (E) ex;

            return Either.Left(typed);
        }
    }

    /**
     * Functional interface allowing checked exceptions with no return value.
     */
    @FunctionalInterface
    public interface ThrowingRunnable<E extends Exception>
    {
        void run() throws E;
    }


    /**
     * Executes the runnable and captures any thrown exception
     * as Either.Left(exception).
     *
     * Success is represented as Either.Right(null).
     */
    public static <E extends Exception> Either<E, Void> TryVoid(ThrowingRunnable<E> runnable)
    {
        Objects.requireNonNull(runnable, "runnable");

        try
        {
            runnable.run();

            return Either.Right(null);
        }
        catch (Exception ex)
        {
            @SuppressWarnings("unchecked")
            E typed = (E) ex;

            return Either.Left(typed);
        }
    }


    /**
     * Create a Left instance.
     */
    public static <L, R> Either<L, R> Left(L value)
    {
        return new Either<>(Objects.requireNonNull(value, "left"), null, true);
    }

    /**
     * Create a Right instance.
     */
    public static <L, R> Either<L, R> Right(R value)
    {
        return new Either<>(null, Objects.requireNonNull(value, "right"), false);
    }

    /**
     * @return true if this instance represents Left.
     */
    public boolean isLeft()
    {
        return isLeft;
    }

    /**
     * @return true if this instance represents Right.
     */
    public boolean isRight()
    {
        return !isLeft;
    }

    /**
     * Retrieve the Left value.
     *
     * Throws if this is a Right.
     */
    public L left()
    {
        if (!isLeft) throw new IllegalStateException("Either is Right");

        return left;
    }

    /**
     * Retrieve the Right value.
     *
     * Throws if this is a Left.
     */
    public R right()
    {
        if (isLeft) throw new IllegalStateException("Either is Left");

        return right;
    }

    /**
     * @return Optional containing Left value if present.
     */
    public Optional<L> tryLeft()
    {
        if (isLeft) return Optional.of(left);

        return Optional.empty();
    }

    /**
     * @return Optional containing Right value if present.
     */
    public Optional<R> tryRight()
    {
        if (!isLeft) return Optional.of(right);

        return Optional.empty();
    }

    /**
     * Fold both branches into a single result.
     *
     * Forces the caller to handle both Left and Right cases.
     * Equivalent to pattern matching.
     */
    public <T> T fold(Function<? super L, ? extends T> onLeft, Function<? super R, ? extends T> onRight)
    {
        Objects.requireNonNull(onLeft, "onLeft");
        Objects.requireNonNull(onRight, "onRight");

        if (isLeft) return onLeft.apply(left);

        return onRight.apply(right);
    }

    /**
     * Execute side-effect if Left.
     * Returns this for fluent chaining.
     */
    public Either<L, R> ifLeft(Consumer<? super L> fn)
    {
        Objects.requireNonNull(fn, "fn");

        if (isLeft) fn.accept(left);

        return this;
    }

    /**
     * Execute side-effect if Right.
     * Returns this for fluent chaining.
     */
    public Either<L, R> ifRight(Consumer<? super R> fn)
    {
        Objects.requireNonNull(fn, "fn");

        if (!isLeft) fn.accept(right);

        return this;
    }

    /**
     * Transform the Left value while preserving Right.
     */
    public <L2> Either<L2, R> mapLeft(Function<? super L, ? extends L2> fn)
    {
        Objects.requireNonNull(fn, "fn");

        if (isLeft) return Either.Left(fn.apply(left));

        return Either.Right(right);
    }

    /**
     * Transform the Right value while preserving Left.
     */
    public <R2> Either<L, R2> mapRight(Function<? super R, ? extends R2> fn)
    {
        Objects.requireNonNull(fn, "fn");

        if (!isLeft) return Either.Right(fn.apply(right));

        return Either.Left(left);
    }

    /**
     * FlatMap the Right branch.
     *
     * Enables chaining computations that themselves return Either.
     */
    public <R2> Either<L, R2> flatMapRight(Function<? super R, Either<L, R2>> fn)
    {
        Objects.requireNonNull(fn, "fn");

        if (!isLeft) return Objects.requireNonNull(fn.apply(right), "flatMap result");

        return Either.Left(left);
    }

    /**
     * FlatMap the Left branch.
     *
     * Useful for transforming or recovering from error states.
     */
    public <L2> Either<L2, R> flatMapLeft(Function<? super L, Either<L2, R>> fn)
    {
        Objects.requireNonNull(fn, "fn");

        if (isLeft) return Objects.requireNonNull(fn.apply(left), "flatMap result");

        return Either.Right(right);
    }

    @Override
    public String toString()
    {
        if (isLeft) return "Either.Left(" + left + ")";

        return "Either.Right(" + right + ")";
    }

    @Override
    public int hashCode()
    {
        if (isLeft) return Objects.hash(true, left);

        return Objects.hash(false, right);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;

        if (!(obj instanceof Either<?, ?> other)) return false;

        if (this.isLeft != other.isLeft) return false;

        if (this.isLeft) return Objects.equals(this.left, other.left);

        return Objects.equals(this.right, other.right);
    }
}
