package com.byteentropy.bulkhead_demo;

import com.byteentropy.bulkhead_demo.service.ExternalApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BulkheadDemoApplicationTests {

    @Autowired
    private ExternalApiService apiService;

    @Test
    void testSemaphoreBulkheadLimit() {
        // High pressure: 50 requests at once to force the 5-limit bulkhead to fail
        int totalRequests = 50;

        List<CompletableFuture<String>> futures = IntStream.range(0, totalRequests)
                .boxed()
                .parallel() // Forces parallel submission
                .map(i -> CompletableFuture.supplyAsync(() -> apiService.slowSemaphoreCall()))
                .toList();

        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long successCount = results.stream().filter(r -> r.contains("Semaphore Success")).count();
        long fallbackCount = results.stream().filter(r -> r.contains("Fallback")).count();

        System.out.println("--- CI Semaphore Results ---");
        System.out.println("Success: " + successCount + " | Fallback: " + fallbackCount);

        // Logic: Fallback MUST have triggered at least once
        assertThat(fallbackCount).as("Bulkhead should have rejected some requests").isPositive();
    }

    @Test
    void testThreadPoolBulkheadIsolation() {
        // High pressure: 30 requests for a pool of 3 + 1 queue
        int totalRequests = 30;

        List<CompletableFuture<String>> futures = IntStream.range(0, totalRequests)
                .boxed()
                .parallel()
                .map(i -> apiService.isolatedThreadCall())
                .toList();

        List<String> results = futures.stream()
                .map(f -> f.handle((res, ex) -> res != null ? res : "Error"))
                .map(CompletableFuture::join)
                .toList();

        long successCount = results.stream().filter(r -> r.contains("Thread Pool Success")).count();
        long fallbackCount = results.stream().filter(r -> r.contains("Fallback")).count();

        System.out.println("--- CI ThreadPool Results ---");
        System.out.println("Success: " + successCount + " | Fallback: " + fallbackCount);

        assertThat(fallbackCount).as("Thread pool should have been saturated").isPositive();
    }
}
