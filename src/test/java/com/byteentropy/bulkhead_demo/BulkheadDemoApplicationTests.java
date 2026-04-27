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

    /**
     * TEST 1: Semaphore Bulkhead (serviceA)
     * Limit is 5. We send 10. We expect roughly 5 to succeed and 5 to fallback.
     */
    @Test
    void testSemaphoreBulkheadLimit() {
        int totalRequests = 10;

        List<CompletableFuture<String>> futures = IntStream.range(0, totalRequests)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> apiService.slowSemaphoreCall()))
                .toList();

        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long successCount = results.stream().filter(r -> r.contains("Semaphore Success")).count();
        long fallbackCount = results.stream().filter(r -> r.contains("Fallback")).count();

        System.out.println("--- Semaphore Results ---");
        System.out.println("Success: " + successCount + " | Fallback: " + fallbackCount);

        assertThat(successCount).isLessThanOrEqualTo(6); // Allowing a tiny margin for OS scheduling
        assertThat(fallbackCount).isGreaterThanOrEqualTo(4);
    }

    /**
     * TEST 2: Thread Pool Bulkhead (serviceB)
     * Pool is 3 + Queue is 1 (Total capacity 4). We send 8.
     */
    @Test
    void testThreadPoolBulkheadIsolation() {
        int totalRequests = 8;

        List<CompletableFuture<String>> futures = IntStream.range(0, totalRequests)
                .mapToObj(i -> apiService.isolatedThreadCall())
                .toList();

        List<String> results = futures.stream()
                .map(f -> f.handle((res, ex) -> res != null ? res : "Exception: " + ex.getMessage()))
                .map(CompletableFuture::join)
                .toList();

        long successCount = results.stream().filter(r -> r.contains("Thread Pool Success")).count();
        long fallbackCount = results.stream().filter(r -> r.contains("Fallback")).count();

        System.out.println("--- ThreadPool Results ---");
        System.out.println("Success: " + successCount + " | Fallback: " + fallbackCount);

        // Capacity is 4. Overflow is handled by fallback.
        assertThat(successCount).isBetween(3L, 5L);
        assertThat(fallbackCount).isGreaterThanOrEqualTo(3);
    }
}