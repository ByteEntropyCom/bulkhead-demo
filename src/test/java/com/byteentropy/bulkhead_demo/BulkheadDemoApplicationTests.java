package com.byteentropy.bulkhead_demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

// Use DEFINED_PORT or RANDOM_PORT to start the real server for the bouncer to work
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BulkheadDemoApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testSemaphoreBulkheadLimit() {
        String url = "http://localhost:" + port + "/semaphore";
        int totalRequests = 30;

        // Use parallelStream to flood the HTTP port
        List<CompletableFuture<String>> futures = IntStream.range(0, totalRequests)
                .boxed()
                .parallel()
                .map(i -> CompletableFuture.supplyAsync(() -> restTemplate.getForObject(url, String.class)))
                .toList();

        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long fallbackCount = results.stream().filter(r -> r.contains("Fallback")).count();
        
        System.out.println("--- CI HTTP Semaphore Results ---");
        System.out.println("Total: " + totalRequests + " | Fallbacks: " + fallbackCount);

        assertThat(fallbackCount).as("Bulkhead must reject requests when hitting the HTTP endpoint").isPositive();
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

        System.out.println("--- CI HTTP ThreadPool Results ---");
        System.out.println("Total: " + totalRequests + " | Fallbacks: " + fallbackCount);

        assertThat(fallbackCount).as("Thread pool should be saturated").isPositive();
    }
}
