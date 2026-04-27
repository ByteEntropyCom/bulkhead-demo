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
    void testSemaphoreBulkheadLimit() throws InterruptedException {
        // WARM UP: Give Resilience4j a moment to initialize in the CI environment
        apiService.slowSemaphoreCall();
        Thread.sleep(1000); 

        int totalRequests = 10;

        // Use parallelStream to ensure high-pressure simultaneous firing
        List<CompletableFuture<String>> futures = IntStream.range(0, totalRequests)
                .parallel() 
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> apiService.slowSemaphoreCall()))
                .toList();

        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long successCount = results.stream().filter(r -> r.contains("Semaphore Success")).count();
        long fallbackCount = results.stream().filter(r -> r.contains("Fallback")).count();

        System.out.println("--- CI Semaphore Results ---");
        System.out.println("Success: " + successCount + " | Fallback: " + fallbackCount);

        // In CI, we just want to prove the bulkhead is working by seeing AT LEAST some fallbacks
        assertThat(fallbackCount).isGreaterThan(0);
        assertThat(successCount).isLessThan(10); 
    }

    @Test
    void testThreadPoolBulkheadIsolation() {
        int totalRequests = 8;

        List<CompletableFuture<String>> futures = IntStream.range(0, totalRequests)
                .parallel()
                .mapToObj(i -> apiService.isolatedThreadCall())
                .toList();

        List<String> results = futures.stream()
                .map(f -> f.handle((res, ex) -> res != null ? res : "Error"))
                .map(CompletableFuture::join)
                .toList();

        long successCount = results.stream().filter(r -> r.contains("Thread Pool Success")).count();
        long fallbackCount = results.stream().filter(r -> r.contains("Fallback")).count();

        System.out.println("--- CI ThreadPool Results ---");
        System.out.println("Success: " + successCount + " | Fallback: " + fallbackCount);

        assertThat(fallbackCount).isGreaterThan(0);
        assertThat(successCount).isLessThan(8);
    }
}
