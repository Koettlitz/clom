package de.dk.util;

import java.util.Objects;
import java.util.function.Function;

/**
 * A <code>Function</code> that throws an exception.
 * This interface is almost equivalent to {@link Function},
 * except it throws an exception.
 *
 * @author David Koettlitz
 * <br>Erstellt am 07.08.2017
 *
 * @see Function
 */
@FunctionalInterface
public interface UnsafeFunction<T, R, E extends Exception> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     *
     * @return the function result
     *
     * @throws E if an exception occurs
     */
    public R apply(T t) throws E;

    /**
     * Returns a composed <code>UnsafeFunction</code> that first applies the {@code before}
     * function to its input, and then applies this <code>UnsafeFunction</code> to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V> the type of input to the {@code before} function, and to the
     *           composed function
     * @param before the function to apply before this function is applied
     *
     * @return a composed function that first applies the {@code before}
     * function and then applies this function
     *
     * @throws NullPointerException if before is null
     *
     * @see #andThen
     * @see Function#compose(Function)
     */
    default <V> UnsafeFunction<V, R, E> compose(UnsafeFunction<? super V, ? extends T, ? extends E> before) {
        Objects.requireNonNull(before);
        return v -> apply(before.apply(v));
    }

    /**
     * Returns a composed <code>UnsafeFunction</code> that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V> the type of output of the {@code after} function, and of the
     *           composed function
     * @param after the function to apply after this function is applied
     *
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     *
     * @throws NullPointerException if after is null
     *
     * @see #compose
     */
    default <V> UnsafeFunction<T, V, E> andThen(UnsafeFunction<? super R, ? extends V, ? extends E> after) {
        Objects.requireNonNull(after);
        return t -> after.apply(apply(t));
    }

}
