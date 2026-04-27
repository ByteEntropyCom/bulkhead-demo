package com.byteentropy.bulkhead_core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.threads.virtual.enabled=true",
    "resilience4j.bulkhead.instances.serviceA.max-concurrent-calls=2",
    "resilience4j.bulkhead.instances.serviceA.max-wait-duration=0",
    "resilience4j.thread-pool-bulkhead.instances.serviceB.max-thread-pool-size=2",
    "resilience4j.thread-pool-bulkhead.instances.serviceB.core-thread-pool-size=1",
    "resilience4j.thread-pool-bulkhead.instances.serviceB.queue-capacity=1"
})
class BulkheadDemoApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testSemaphoreBulkheadLimit() {
        runConcurrentTest("/semaphore", "Semaphore");
    }

    @Test
    void testThreadPoolBulkheadIsolation() {
        runConcurrentTest("/threadpool", "ThreadPool");
    }

    private void runConcurrentTest(String path, String label) {
        String url = "http://localhost:" + port + path;
        int totalRequests = 20;

        // Use a dedicated pool to ensure all 20 requests hit the server at once
        try (ExecutorService executor = Executors.newFixedThreadPool(totalRequests)) {
            List<CompletableFuture<String>> futures = new ArrayList<>();

            for (int i = 0; i < totalRequests; i++) {
                futures.add(CompletableFuture.supplyAsync(() -> 
                    restTemplate.getForObject(url, String.class), executor));
            }

            List<String> results = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            long fallbackCount = results.stream()
                    .filter(r -> r != null && r.contains("Fallback"))
                    .count();

            System.out.println("--- CI " + label + " Fallbacks: " + fallbackCount + " ---");

            assertThat(fallbackCount)
                .as(label + " Bulkhead should have triggered")
                .isGreaterThan(0);
        }
    }
}
