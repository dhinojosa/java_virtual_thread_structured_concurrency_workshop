package com.evolutionnext.virtualthreads;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

public class VirtualThreadTest {
    @Test
    void testVirtualThreadUnstarted() throws InterruptedException {
        Thread unstartedThread = Thread.ofVirtual().unstarted(
            new Runnable() {
                @Override
                public void run() {
                    System.out.printf("Starting Thread in %s", Thread.currentThread());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

        System.out.println("Starting Thread");
        unstartedThread.start();

        System.out.println("Waiting for Thread to finish");
        unstartedThread.join();
    }

    @Test
    void testVirtualThreadStarted() throws InterruptedException {
        Thread startedThread = Thread.ofVirtual().start(() -> {
            System.out.printf("Starting Thread in %s", Thread.currentThread());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("Waiting for Thread to finish");
        startedThread.join();
    }

    @Test
    void testVirtualThreadNamedAndStarted() throws InterruptedException {
        Thread startedThread = Thread.ofVirtual().name("custom-virtual-thread").start(() -> {
            System.out.printf("Starting Thread in %s", Thread.currentThread());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("Waiting for Thread to finish");
        startedThread.join();
    }

    @Test
    void testVirtualThreadInTryWithResources() {
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<Long> future = executorService.submit(() -> {
                Thread currentThread = Thread.currentThread();
                System.out.printf("Thread %s in the future", currentThread);
                System.out.format("Is our thread virtual? %b\n", currentThread.isVirtual());
                return 10L;
            });

            System.out.println("Submitted");
            System.out.println(future.get(4, TimeUnit.SECONDS));

        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testVirtualThreadWithAPoolFactory() throws ExecutionException, InterruptedException {
        ThreadFactory tf =
            Thread.ofVirtual().name("virtual-with-executors").factory();
        try (ExecutorService executorService = Executors.newThreadPerTaskExecutor(tf)) {
            Future<Integer> future = executorService.submit(() -> {
                System.out.format("Running in Thread: %s\n",
                    Thread.currentThread());
                System.out.format("Is our thread virtual? %b\n",
                    Thread.currentThread().isVirtual());
                return 100;
            });
            System.out.println(future.get());
        }
    }

    @Test
    void testFutureWithNoGetAndAnError() throws InterruptedException {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        pool.submit(() -> {
            System.out.println("Running in Thread:");
            // Exception thrown here
            int x = 1 / 0;
            System.out.println(x);
        });
        Thread.sleep(1000);
        pool.shutdown();
    }

    @Test
    void testWithCompletableFuture() {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("Running in Thread:");
            int x = 1 / 0;
            System.out.println(x);
            return 100;
        }, pool);
        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                System.out.println("There was an error");
                throwable.printStackTrace();
            } else {
                System.out.println("Result is " + result);
            }
        });
        pool.shutdown();
        System.out.println("Done");

    }

    @Test
    void testStackTraceWithVirtualThread() throws InterruptedException {
        Thread t = Thread.ofVirtual().start(() -> {
            System.out.println("Running in Thread:");
            int x = 1 / 0;
            System.out.println(x);
        });
        Thread.sleep(1000);
    }

    @Test
    void testComposingVirtualThreads() throws InterruptedException {
        Thread startedThread = Thread.ofVirtual().start(() -> {
            final var city = WeatherStation.getCity();
            Thread innerThread = Thread.ofVirtual().start(() -> {
                final var temperature = WeatherStation.getTemperature(city);
                System.out.printf("The temperature for %s is %d", city, temperature);
            });
            try {
                innerThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("Waiting for Thread to finish");
        startedThread.join();
    }
}
