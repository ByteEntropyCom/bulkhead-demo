package com.byteentropy.bulkhead_core.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class ExternalApiService {

    @Bulkhead(name = "serviceA", fallbackMethod = "fallbackSemaphore")
    public String slowSemaphoreCall() {
        simulateDelay();
        return "Semaphore Success By Virtual Thread: " + Thread.currentThread().isVirtual();
    }

    // FIX: Body must be non-blocking to the caller. 
    // The Bulkhead will intercept the returned CompletableFuture.
    @Bulkhead(name = "serviceB", type = Bulkhead.Type.THREADPOOL, fallbackMethod = "fallbackThreadPool")
    public CompletableFuture<String> isolatedThreadCall() {
        return CompletableFuture.supplyAsync(() -> {
            simulateDelay(); // The delay now happens INSIDE the bulkhead pool
            return "Thread Pool Success on: " + Thread.currentThread().getName();
        });
    }

    private void simulateDelay() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String fallbackSemaphore(Throwable t) {
        return "Fallback: Semaphore limit reached!";
    }

    public CompletableFuture<String> fallbackThreadPool(Throwable t) {
        return CompletableFuture.completedFuture("Fallback: Thread pool saturated!");
    }
}
