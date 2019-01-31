package com.king.services.scorestore.processor.impl;

import com.king.services.scorestore.model.User;
import com.king.services.scorestore.processor.QueueProcessor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * InMemory Implementation of the QueueProcessor for Async Processing of User Objects
 */
public class InMemoryQueueProcessorImpl implements QueueProcessor<User> {

    private final BlockingQueue<User> queue;

    public InMemoryQueueProcessorImpl() {
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void put(User user) throws InterruptedException {
        queue.put(user);
    }

    @Override
    public User take() throws InterruptedException {
        return queue.take();
    }
}
