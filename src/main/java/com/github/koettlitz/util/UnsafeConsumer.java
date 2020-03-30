package com.github.koettlitz.util;

import java.util.function.Consumer;

/**
 * A <code>Consumer</code> that throws an exception.
 * This interface is almost equivalent to {@link Consumer},
 * except it throws an exception.
 *
 * @author David Koettlitz
 * <br>Erstellt am 07.08.2017
 *
 * @see Consumer
 */
@FunctionalInterface
public interface UnsafeConsumer<T, E extends Exception> {
    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     *
     * @throws E if an exception occurs
     */
    public void accept(T value) throws E;

    /**
     * Returns a composed {@code UnsafeConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     *
     * @return a composed {@code UnsafeConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     *
     * @throws NullPointerException if {@code after} is <code>null</code>
     */
    default UnsafeConsumer<T, E> andThen(UnsafeConsumer<? super T, ? extends E> after) {
        return t -> {
            accept(t);
            after.accept(t);
        };
    }

    /**
     * Returns a composed <code>UnsafeConsumers</code> that performs, in sequence, the
     * <code>before</code> operation followed by this operation.
     * If performing the operation of <code>before</code> throws an exception
     * this operation is not performed.
     *
     * @param before The operation to perform before this operation
     *
     * @return A composed <code>UnsafeConsumer</code> that performs, in sequence, the
     * <code>before</code> operation followed by this operation.
     */
    default UnsafeConsumer<T, E> compose(UnsafeConsumer<? super T, ? extends E> before) {
        return t -> {
            before.accept(t);
            accept(t);
        };
    }

}
