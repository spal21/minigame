package com.king.services.scorestore.processor;

/**
 * Handles Queue Processing Responsibilities
 * @param <T>
 */
public interface QueueProcessor<T> {

    /**
     * Adds an Object to Tail of the Queue.
     *
     * @param t Object
     * @throws InterruptedException
     */
    void put(T t) throws InterruptedException;

    /**
     * Returns the Object from the Head of the Queue.
     *
     * @return Object
     * @throws InterruptedException
     */
    T take() throws InterruptedException;
}
