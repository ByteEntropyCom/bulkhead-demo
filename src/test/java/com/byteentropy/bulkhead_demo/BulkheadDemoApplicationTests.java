package com.byteentropy.bulkhead_demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// FORCE the limits here so CI cannot ignore them
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
        String url = "http://localhost:" + port + "/semaphore";
        // We fired 30; with a limit of 2, we MUST see fallbacks
        int totalRequests = 20;

        List<CompletableFuture<String>> futures = IntStream.range(0, totalRequests)
                .boxed()
                .parallel()
                .map(i -> CompletableFuture.supplyAsync(() -> restTemplate.getForObject(url, String.class)))
                .toList();

        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long fallbackCount = results.stream().filter(r -> r.contains("Fallback")).count();
        
        System.out.println("--- CI Semaphore Fallbacks: " + fallbackCount + " ---");

        assertThat(fallbackCount).as("Semaphore Bulkhead should have triggered").isGreaterThan(0);
    }

    @Test
    void testThreadPoolBulkheadIsolation() {
        String url = "http://localhost:" + port + "/threadpool";
        int totalRequests = 20;

        List<CompletableFuture<String>> futures = IntStream.range(0, totalRequests)
                .boxed()
                .parallel()
                .map(i -> CompletableFuture.supplyAsync(() -> restTemplate.getForObject(url, String.class)))
                .toList();

        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long fallbackCount = results.stream().filter(r -> r.contains("Fallback")).count();

        System.out.println("--- CI ThreadPool Fallbacks: " + fallbackCount + " ---");

        assertThat(fallbackCount).as("Thread Pool Bulkhead should have triggered").isGreaterThan(0);
    }
}
