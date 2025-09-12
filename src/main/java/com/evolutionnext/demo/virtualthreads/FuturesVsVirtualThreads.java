package com.evolutionnext.demo.virtualthreads;


import java.util.concurrent.*;

public class FuturesVsVirtualThreads {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Future (no get) ===");
        usingFutureWithoutGet();

        System.out.println("\n=== Virtual Thread ===");
        usingVirtualThread();
    }

    static void usingFutureWithoutGet() {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        pool.submit(() -> {
            // Exception thrown here
            int x = 1 / 0;
            System.out.println(x);
        });
        pool.shutdown();
    }

    static void usingVirtualThread() throws Exception {
        Thread t = Thread.ofVirtual().start(() -> {
            int x = 1 / 0;
            System.out.println(x);
        });
        t.join();
    }
}
