package com.king.services.scorestore.app;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Launcher for Http based Minigame for Storing scores
 */
public class Launcher {

    private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());

    public static void main(String[] args) throws InterruptedException {
        Application application = new Application();
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread("shutdown-hook") {
            @Override
            public void run() {
                try {
                    application.closeServer();
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Exception encountered while shutting Down Server.");
                }
                latch.countDown();
            }
        });
        application.run();
        latch.await();
    }
}