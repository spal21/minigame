package com.king.services.scorestore.processor;

/**
 * Handles Queue Consumer Responsibilities
 */
public interface QueueConsumer {

    /**
     * Starts a Queue Consumer
     */
    void start();

    /**
     * Stops Queue Consumer
     *
     * @throws InterruptedException
     */
    void stop() throws InterruptedException;
}
