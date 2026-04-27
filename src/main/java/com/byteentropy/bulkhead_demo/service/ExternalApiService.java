package com.byteentropy.bulkhead_demo.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class ExternalApiService {

    @Bulkhead(name = "serviceA", fallbackMethod = "fallbackSemaphore")
    public String slowSemaphoreCall() {
        simulateDelay();
        boolean isVirtual = Thread.currentThread().isVirtual();
        return "Semaphore Success By Virtual Thread: " + isVirtual;
    }

    @Bulkhead(name = "serviceB", type = Bulkhead.Type.THREADPOOL, fallbackMethod = "fallbackThreadPool")
    public CompletableFuture<String> isolatedThreadCall() {
        simulateDelay();
        return CompletableFuture.completedFuture("Thread Pool Success on: " + Thread.currentThread().getName());
    }

    private void simulateDelay() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String fallbackSemaphore(Throwable t) {
        // Log the specific error (BulkheadFullException)
        System.err.println("Semaphore Fallback triggered: " + t.getMessage());
        return "Fallback: Semaphore limit reached!";
    }

    public CompletableFuture<String> fallbackThreadPool(Throwable t) {
        return CompletableFuture.completedFuture("Fallback: Thread pool saturated!");
    }
}