package com.king.services.scorestore.processor.impl;

import com.king.services.scorestore.model.UserScoreDetails;
import com.king.services.scorestore.processor.QueueProcessor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * InMemory Implementation of the QueueProcessor for Async Processing of UserScore Info Objects
 */
public class InMemoryQueueProcessorImpl implements QueueProcessor<UserScoreDetails> {

    private final BlockingQueue<UserScoreDetails> queue;

    public InMemoryQueueProcessorImpl() {
        this.queue = new ArrayBlockingQueue<>(1000);
    }

    @Override
    public void put(UserScoreDetails userScoreDetails) throws InterruptedException {
        queue.put(userScoreDetails);
    }

    @Override
    public UserScoreDetails take() throws InterruptedException {
        return queue.take();
    }
}
